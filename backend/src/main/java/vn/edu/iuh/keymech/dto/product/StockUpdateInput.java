package vn.edu.iuh.keymech.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Input điều chỉnh tồn kho (A-01). Thao tác chủ đích, tách riêng khỏi form sửa
 * thông tin để tránh ghi đè tồn kho mới do khách checkout trong lúc form mở.
 */
public record StockUpdateInput(
        @NotNull(message = "Vui lòng nhập tồn kho") @PositiveOrZero(message = "Tồn kho phải lớn hơn hoặc bằng 0")
        int stockQuantity) {}
