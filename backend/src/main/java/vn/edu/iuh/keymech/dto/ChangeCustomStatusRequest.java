package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

/** Staff hoặc Admin chuyển trạng thái yêu cầu Custom (A-07). */
public record ChangeCustomStatusRequest(
    @NotNull(message = "Vui lòng chọn trạng thái mới")
    CustomStatus status,

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    String note
) {}
