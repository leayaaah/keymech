package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

public record CustomRequestInput(
        @NotBlank @Pattern(regexp = "60%|65%|75%|TKL|Fullsize", message = "Layout không hợp lệ") String layout,
        @NotBlank @Pattern(regexp = "Linear|Tactile|Clicky", message = "Loại switch không hợp lệ") String switchType,
        @NotBlank(message = "Vui lòng nhập tên switch") @Size(max = 70) String switchName,
        @Pattern(regexp = "Nhựa|Nhôm|Gỗ|Acrylic", message = "Chất liệu case không hợp lệ") String caseMaterial,
        @Size(max = 120) String keycapSet,
        @Pattern(regexp = "Không|RGB|Đơn sắc", message = "Loại đèn LED không hợp lệ") String ledType,
        Boolean lubeSwitch, Boolean hotSwap,
        @Min(value = 500000, message = "Ngân sách tối thiểu 500.000 đ") @Digits(integer = 12, fraction = 0) BigDecimal budget,
        @Size(max = 1000) String note) {

    public record Quote(
            @NotNull @Min(value = 1, message = "Giá báo phải lớn hơn 0")
            @Digits(integer = 12, fraction = 0) BigDecimal quotedPrice,
            @Size(max = 500) String quoteNote) {}

    public record Accept(@NotNull @Min(1) @Digits(integer = 12, fraction = 0) BigDecimal quotedPrice) {}

    public record Decline(@Size(max = 500) String note) {}

    public record Transition(@NotNull CustomStatus status, @Size(max = 500) String note) {}
}
