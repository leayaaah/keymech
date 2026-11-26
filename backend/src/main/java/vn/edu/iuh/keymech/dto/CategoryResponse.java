package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;

public record CategoryResponse(Long id, String name, String slug, VisibilityStatus status) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getStatus());
    }

    public record Create(
            @NotBlank(message = "Vui lòng nhập tên danh mục") @Size(max = 100) String name,
            @NotBlank @Size(max = 120)
            @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*", message = "Slug không hợp lệ") String slug) {}

    public record Visibility(@NotNull(message = "Vui lòng chọn trạng thái") VisibilityStatus status) {}
}
