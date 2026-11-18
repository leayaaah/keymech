package vn.edu.iuh.keymech.dto.product;

import java.math.BigDecimal;

/**
 * Một dòng trong danh sách sản phẩm (G-01).
 * Mô tả rút gọn 120 ký tự để danh sách gọn nhẹ; chi tiết đầy đủ ở G-02.
 */
public record ProductsResponse(
        Long id,
        String name,
        String brand,
        BigDecimal price,
        String layout,
        String switchType,
        int stockQuantity,
        String imageUrl,
        String descriptionShort
) {
}
