package vn.edu.iuh.keymech.dto.product;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;

public record StaffProductInput(
        @NotBlank(message = "Vui lòng nhập tên sản phẩm") @Size(max = 200) String name,
        @NotNull(message = "Vui lòng chọn danh mục") Long categoryId,
        @Size(max = 100) String brand,
        @Size(max = 20) String layout,
        @Size(max = 30) String switchType,
        @NotNull @Min(value = 1, message = "Giá bán phải lớn hơn 0")
        @Digits(integer = 12, fraction = 0) BigDecimal price,
        @NotNull @Min(value = 0, message = "Tồn kho phải lớn hơn hoặc bằng 0") Integer stockQuantity,
        @Size(max = 16000) String description,
        @Size(max = 16000) String specs,
        MultipartFile image) {}
