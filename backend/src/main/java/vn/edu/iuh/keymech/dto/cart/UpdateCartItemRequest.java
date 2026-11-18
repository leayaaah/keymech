package vn.edu.iuh.keymech.dto.cart;

import jakarta.validation.constraints.Min;

/**
 * Sửa số lượng một mặt hàng trong giỏ (G-03).
 */
public record UpdateCartItemRequest(
        @Min(value = 1, message = "Số lượng phải từ 1") int quantity
) {
}
