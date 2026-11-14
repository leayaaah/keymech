package vn.edu.iuh.keymech.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

/** Yêu cầu Custom. {@code customer} chỉ có ở phía Staff, phía Customer là null và bị bỏ khỏi JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomRequestResponse(
    Long id,
    String requestCode,
    String layout,
    String caseMaterial,
    String switchType,
    String keycapSet,
    boolean lubing,
    boolean hotSwap,
    String rgbOption,
    BigDecimal budget,
    String note,
    BigDecimal quotedPrice,
    CustomStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    CustomerSummary customer
) {}
