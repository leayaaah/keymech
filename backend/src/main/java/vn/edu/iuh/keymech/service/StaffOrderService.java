package vn.edu.iuh.keymech.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.dto.StaffOrderResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.Order;
import vn.edu.iuh.keymech.model.OrderItem;
import vn.edu.iuh.keymech.model.enums.OrderStatus;
import vn.edu.iuh.keymech.repository.OrderRepository;
import vn.edu.iuh.keymech.repository.OrderStockRepository;

@Service
public class StaffOrderService {

    private static final BigDecimal MAX_ORDER_TOTAL = new BigDecimal("99999999999999");

    private final OrderRepository orders;
    private final OrderStockRepository stock;

    public StaffOrderService(OrderRepository orders, OrderStockRepository stock) {
        this.orders = orders;
        this.stock = stock;
    }

    @Transactional(readOnly = true)
    public PageResponse<StaffOrderResponse.Summary> list(String keyword, LocalDate fromDate,
            LocalDate toDate, OrderStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw invalid("Trang phải từ 0 và kích thước trang phải từ 1 đến 50");
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw invalid("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        String term = keyword == null || keyword.isBlank() ? null : keyword.strip();
        LocalDateTime fromTime = fromDate == null ? null : fromDate.atStartOfDay();
        // orders.order_date có độ chính xác đến giây và lưu theo giờ nghiệp vụ.
        LocalDateTime toTime = toDate == null ? null : toDate.atTime(23, 59, 59);
        PageRequest pageable = PageRequest.of(page, size);
        if (pageable.getOffset() > Integer.MAX_VALUE) {
            long total = orders.countStaffOrders(term, fromTime, toTime, status);
            return PageResponse.of(new PageImpl<>(List.of(), pageable, total));
        }
        return PageResponse.of(orders.findStaffOrders(term, fromTime, toTime, status, pageable)
            .map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public StaffOrderResponse detail(long id) {
        return toDetail(orders.findStaffDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng")));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StaffOrderResponse updateQuantity(long id, long itemId, int quantity) {
        if (quantity < 1) {
            throw invalid("Số lượng phải là số nguyên dương");
        }
        Order order = orders.findLockedById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        OrderItem line = order.getItems().stream()
            .filter(item -> item.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dòng hàng trong đơn"));
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("ORDER_NOT_EDITABLE", "Trạng thái đơn không cho phép sửa số lượng");
        }

        int delta = quantity - line.getQuantity();
        BigDecimal total = order.getItems().stream()
            .map(item -> lineTotal(item, item == line ? quantity : item.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(MAX_ORDER_TOTAL) > 0) {
            throw invalid("Số lượng làm tổng tiền đơn hàng vượt giới hạn");
        }
        Long productId = line.getProduct().getId();
        if (delta > 0 && stock.decreaseStock(productId, delta) != 1) {
            throw new BusinessException("OUT_OF_STOCK", "Sản phẩm không còn đủ hàng");
        }
        if (delta < 0 && stock.increaseStock(productId, -delta) != 1) {
            throw new BusinessException("OUT_OF_STOCK", "Không thể hoàn tồn cho sản phẩm");
        }
        line.setQuantity(quantity);
        order.setTotalAmount(total);
        return toDetail(order);
    }

    private StaffOrderResponse.Summary toSummary(Order order) {
        var customer = order.getUser();
        return new StaffOrderResponse.Summary(order.getId(), order.getOrderCode(), customer.getId(),
            customer.getFullName(), customer.getEmail(), order.getOrderDate(), order.getStatus(),
            order.getPaymentStatus(), order.getTotalAmount());
    }

    private StaffOrderResponse toDetail(Order order) {
        var customer = order.getUser();
        var items = order.getItems().stream().sorted(Comparator.comparing(OrderItem::getId))
            .map(item -> new StaffOrderResponse.Item(item.getId(), item.getProduct().getId(),
                item.getProduct().getName(), item.getQuantity(), item.getUnitPrice(), lineTotal(item, item.getQuantity())))
            .toList();
        return new StaffOrderResponse(order.getId(), order.getOrderCode(), customer.getId(), customer.getFullName(),
            customer.getEmail(), order.getOrderDate(), order.getReceiverName(), order.getReceiverPhone(),
            order.getShippingAddress(), order.getPaymentMethod(), order.getPaymentStatus(),
            order.getStatus(), order.getTotalAmount(), items);
    }

    private static BigDecimal lineTotal(OrderItem item, int quantity) {
        return item.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }
}
