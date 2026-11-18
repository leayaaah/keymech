package vn.edu.iuh.keymech.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import vn.edu.iuh.keymech.model.enums.OrderStatus;
import vn.edu.iuh.keymech.model.enums.PaymentMethod;
import vn.edu.iuh.keymech.model.enums.PaymentStatus;

public record CustomerOrderResponse(
    Long id, String orderCode,
    @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ss") LocalDateTime orderDate,
    String receiverName, String receiverPhone, String shippingAddress, PaymentMethod paymentMethod,
    PaymentStatus paymentStatus, OrderStatus status, BigDecimal totalAmount, List<OrderLineResponse> items
) {
    public record Summary(
        Long id, String orderCode,
        @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ss") LocalDateTime orderDate,
        OrderStatus status, PaymentMethod paymentMethod, PaymentStatus paymentStatus, BigDecimal totalAmount
    ) {}
}
