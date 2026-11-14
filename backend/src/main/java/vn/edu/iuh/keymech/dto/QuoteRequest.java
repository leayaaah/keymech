package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Staff nhập báo giá (A-07): giá phải lớn hơn 0. */
public record QuoteRequest(
    @NotNull(message = "Vui lòng nhập giá báo")
    @DecimalMin(value = "1", message = "Giá báo phải lớn hơn 0")
    @Digits(integer = 12, fraction = 0, message = "Giá báo phải là số nguyên đồng, tối đa 12 chữ số")
    BigDecimal quotedPrice
) {}
