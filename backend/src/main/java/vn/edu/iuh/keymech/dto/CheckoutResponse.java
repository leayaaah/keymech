package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import vn.edu.iuh.keymech.model.enums.OrderStatus;
import vn.edu.iuh.keymech.model.enums.PaymentStatus;

public record CheckoutResponse(
    Long id,
    String orderCode,
    OrderStatus status,
    PaymentStatus paymentStatus,
    BigDecimal totalAmount
) {}
