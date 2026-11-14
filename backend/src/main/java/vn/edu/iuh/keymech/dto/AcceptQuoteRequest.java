package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Khách đồng ý báo giá (C-06). Gửi kèm giá đang xem; lệch giá hiện tại thì 409 QUOTE_CHANGED,
 * tránh đồng ý nhầm giá cũ khi Staff vừa báo lại.
 */
public record AcceptQuoteRequest(
    @NotNull(message = "Thiếu giá báo đang xem")
    BigDecimal quotedPrice
) {}
