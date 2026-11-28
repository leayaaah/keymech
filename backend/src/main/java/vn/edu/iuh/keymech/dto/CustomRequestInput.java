package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

public record CustomRequestInput(
        @NotNull(message = "Vui lòng chọn kit") @Positive Long kitProductId,
        @NotNull(message = "Vui lòng chọn switch") @Positive Long switchProductId,
        @NotNull(message = "Vui lòng chọn bộ keycap") @Positive Long keycapProductId,
        @Positive Long stabilizerProductId,
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
