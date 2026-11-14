package vn.edu.iuh.keymech.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

/**
 * Một dòng timeline. {@code actor}: phía Customer chỉ là "Bạn" hoặc "Cửa hàng" (không lộ tên Staff);
 * phía Staff là họ tên kèm vai trò. {@code oldStatus} null ở dòng khởi tạo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomHistoryResponse(
    Long id,
    CustomStatus oldStatus,
    CustomStatus newStatus,
    String note,
    String actor,
    LocalDateTime changedAt
) {}
