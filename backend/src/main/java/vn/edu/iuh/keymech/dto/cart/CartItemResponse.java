package vn.edu.iuh.keymech.dto.cart;

import java.math.BigDecimal;

/**
 * Một mặt hàng trong giỏ hàng (G-03).
 * Giá lấy lại từ CSDL; mặt hàng đã ẩn được đánh dấu unavailable.
 */
public record CartItemResponse(
        Long productId,
        String name,
        String imageUrl,
        BigDecimal price,
        int quantity,
        BigDecimal subtotal,
        int stockQuantity,
        boolean available
) {
}