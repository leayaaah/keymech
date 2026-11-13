package vn.edu.iuh.keymech.dto.cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response giỏ hàng (G-03).
 * Không lưu giá trong Session — giá luôn lấy lại từ CSDL.
 */
public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal totalAmount
) {
}