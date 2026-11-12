package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
    @NotBlank @Email String email,
    @NotBlank String code,
    @NotBlank @Size(min = 8) String newPassword
) {}