package vn.edu.iuh.keymech.dto.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

/**
 * Tham số lọc và phân trang danh sách sản phẩm (G-01).
 * Không bắt buộc; null = không lọc theo trường đó.
 * page/size null sẽ được Service gán mặc định 0 và 12.
 */
public record ProductsFilterRequest(
        Long categoryId,
        String brand,
        String layout,
        @DecimalMin(value = "0", message = "Giá tối thiểu không được âm") BigDecimal minPrice,
        @DecimalMin(value = "0", message = "Giá tối đa không được âm") BigDecimal maxPrice,
        @Size(max = 100, message = "Từ khóa tối đa 100 ký tự") String keyword,
        String sort,
        Integer page,
        Integer size
) {
}
