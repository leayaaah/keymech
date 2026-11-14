package vn.edu.iuh.keymech.dto;

import java.util.List;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

/**
 * Chi tiết yêu cầu kèm timeline. {@code allowedNext} là các trạng thái người xem được phép chuyển tới
 * (Customer: đồng ý, hủy; Staff: các bước hợp lệ trừ bước khách đồng ý), để giao diện không phải
 * tự lặp lại bảng chuyển trạng thái.
 */
public record CustomRequestDetailResponse(
    CustomRequestResponse request,
    List<CustomHistoryResponse> history,
    List<CustomStatus> allowedNext
) {}
