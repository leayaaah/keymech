package vn.edu.iuh.keymech.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Thêm sản phẩm vào giỏ (G-03).
 * Số lượng >= 1; không vượt tồn kho (kiểm ở Service).
 */
public record AddToCartRequest(
        @NotNull(message = "Thiếu mã sản phẩm") Long productId,
        @Min(value = 1, message = "Số lượng phải từ 1") int quantity
) {
}
