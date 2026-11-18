package vn.edu.iuh.keymech.dto.product;

import java.math.BigDecimal;

/**
 * Chi tiết sản phẩm (G-02).
 * Hiển thị đầy đủ: tên, ảnh, giá, mô tả, thông số, tồn kho.
 */
public record ProductsDetailResponse(
        Long id,
        String name,
        String brand,
        BigDecimal price,
        String layout,
        String switchType,
        int stockQuantity,
        String imageUrl,
        String description,
        String specs
) {
}