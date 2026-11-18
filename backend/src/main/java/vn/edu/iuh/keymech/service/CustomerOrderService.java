package vn.edu.iuh.keymech.service;

import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.CustomerOrderResponse;
import vn.edu.iuh.keymech.dto.OrderLineResponse;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.Order;
import vn.edu.iuh.keymech.repository.OrderRepository;

@Service
public class CustomerOrderService {

    private static final Sort ORDER_SORT = Sort.by(Sort.Direction.DESC, "orderDate", "id");

    private final OrderRepository orders;

    public CustomerOrderService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerOrderResponse.Summary> list(long userId, int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Trang phải từ 0 và kích thước trang phải từ 1 đến 50");
        }
        PageRequest pageable = PageRequest.of(page, size, ORDER_SORT);
        if (pageable.getOffset() > Integer.MAX_VALUE) {
            return PageResponse.of(new PageImpl<>(List.of(), pageable, orders.countByUserId(userId)));
        }
        return PageResponse.of(orders.findByUserId(userId, pageable).map(order ->
            new CustomerOrderResponse.Summary(order.getId(), order.getOrderCode(), order.getOrderDate(),
                order.getStatus(), order.getPaymentMethod(), order.getPaymentStatus(), order.getTotalAmount())));
    }

    @Transactional(readOnly = true)
    public CustomerOrderResponse detail(long userId, long id) {
        if (id < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Mã đơn hàng phải là số nguyên dương");
        }
        Order order = orders.findByIdAndUserId(id, userId).orElseThrow(() -> {
            if (orders.existsById(id)) {
                return new AccessDeniedException("Bạn không có quyền xem đơn hàng này");
            }
            return new ResourceNotFoundException("Không tìm thấy đơn hàng");
        });
        return new CustomerOrderResponse(order.getId(), order.getOrderCode(), order.getOrderDate(),
            order.getReceiverName(), order.getReceiverPhone(), order.getShippingAddress(), order.getPaymentMethod(),
            order.getPaymentStatus(), order.getStatus(), order.getTotalAmount(),
            OrderLineResponse.fromItems(order.getItems()));
    }
}
