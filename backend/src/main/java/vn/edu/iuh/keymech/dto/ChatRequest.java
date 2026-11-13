package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @NotBlank(message = "Vui lòng nhập câu hỏi")
    @Size(max = 500, message = "Câu hỏi tối đa 500 ký tự") String message
) {}
