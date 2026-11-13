package vn.edu.iuh.keymech.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.CheckoutRequest;
import vn.edu.iuh.keymech.dto.CheckoutResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.model.Order;
import vn.edu.iuh.keymech.model.OrderItem;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.OrderRepository;
import vn.edu.iuh.keymech.repository.OrderStockRepository;
import vn.edu.iuh.keymech.repository.ProductRepository;
import vn.edu.iuh.keymech.repository.UserRepository;
import vn.edu.iuh.keymech.util.CodeGenerator;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderStockRepository stockRepository;
    private final OrderRepository orderRepository;

    public OrderService(UserRepository userRepository, ProductRepository productRepository,
                        OrderStockRepository stockRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.orderRepository = orderRepository;
    }

    /** Transaction commit hoàn tất trước khi Controller xóa giỏ hoặc gửi email. */
    @Transactional
    public PlacedOrder placeOrder(Long userId, CheckoutRequest request, Map<Long, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "EMPTY_CART", "Giỏ hàng đang trống");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AccessDeniedException("Tài khoản không hợp lệ"));
        if (!user.isEnabled() || user.getRole() != Role.CUSTOMER) {
            throw new AccessDeniedException("Tài khoản không có quyền đặt hàng");
        }

        Order order = new Order();
        order.setOrderCode(CodeGenerator.orderCode());
        order.setUser(user);
        order.setReceiverName(request.receiverName().trim());
        order.setReceiverPhone(request.receiverPhone().trim());
        order.setShippingAddress(request.shippingAddress().trim());
        order.setPaymentMethod(request.paymentMethod());
        order.setOrderDate(LocalDateTime.now());

        List<OrderLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        // Khóa các dòng sản phẩm cùng thứ tự để giảm nguy cơ deadlock giữa các giỏ.
        for (Long productId : cart.keySet().stream().sorted(Comparator.nullsFirst(Long::compareTo)).toList()) {
            Integer quantity = cart.get(productId);
            if (productId == null || quantity == null || quantity < 1) {
                throw outOfStock();
            }
            Product product = productRepository.findById(productId).orElseThrow(this::outOfStock);
            if (stockRepository.decreaseStock(productId, quantity) != 1) {
                throw outOfStock();
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            order.getItems().add(item);
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            total = total.add(lineTotal);
            lines.add(new OrderLine(product.getName(), quantity, product.getPrice(), lineTotal));
        }
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        return new PlacedOrder(
            new CheckoutResponse(saved.getId(), saved.getOrderCode(), saved.getStatus(),
                                 saved.getPaymentStatus(), saved.getTotalAmount()),
            user.getEmail(), order.getOrderDate(), request.receiverName().trim(),
            request.receiverPhone().trim(), request.shippingAddress().trim(),
            request.paymentMethod().name(), List.copyOf(lines));
    }

    private BusinessException outOfStock() {
        return new BusinessException("OUT_OF_STOCK", "Sản phẩm không còn đủ hàng");
    }

    public record OrderLine(String productName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}

    public record PlacedOrder(CheckoutResponse response, String toEmail, LocalDateTime orderDate,
                              String receiverName, String receiverPhone, String shippingAddress,
                              String paymentMethod, List<OrderLine> items) {}
}
