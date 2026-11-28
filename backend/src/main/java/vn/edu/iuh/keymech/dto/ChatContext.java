package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Các thông số Custom đã trích từ hội thoại. */
public record ChatContext(
    @Pattern(regexp = "Fullsize|TKL|75%|65%|60%|40%") String layout,
    @DecimalMin("0") @Digits(integer = 18, fraction = 2) BigDecimal budget,
    Boolean quiet,
    @Size(max = 50) @Pattern(regexp = ChatContext.SWITCH_NAME) String switchName
) {
    public static final String SWITCH_NAME =
        "(?:(?:cherry(?: mx)?|gateron|kailh(?: box| speed)?|akko|outemu) )?(?:silent )?"
            + "(?:red|brown|blue|yellow|black|green|white|silver|purple|oil king|speed silver|durock t1)";
}
