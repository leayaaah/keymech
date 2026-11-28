package vn.edu.iuh.keymech.dto.product;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;

/**
 * Input sửa thông tin sản phẩm (A-01) — KHÔNG chứa tồn kho.
 * Tồn kho chỉ đổi bằng endpoint riêng PATCH /api/staff/products/{id}/stock
 * để tránh form sửa ghi đè tồn kho đã bị khách checkout trong lúc mở form.
 */
public record UpdateProductInput(
        @NotBlank(message = "Vui lòng nhập tên sản phẩm") @Size(max = 200) String name,
        @NotNull(message = "Vui lòng chọn danh mục") Long categoryId,
        @Size(max = 100) String brand,
        @Size(max = 20) String layout,
        @Size(max = 30) String switchType,
        @NotNull(message = "Vui lòng nhập giá bán") @Min(value = 1, message = "Giá bán phải lớn hơn 0")
        @Digits(integer = 12, fraction = 0) BigDecimal price,
        @Size(max = 16000) String description,
        @Size(max = 16000) String specs,
        MultipartFile image) {}
