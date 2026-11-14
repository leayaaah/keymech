package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.Size;

/** Khách hủy yêu cầu (PENDING) hoặc từ chối báo giá (QUOTED). Ghi chú không bắt buộc. */
public record DeclineRequest(
    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    String note
) {}
