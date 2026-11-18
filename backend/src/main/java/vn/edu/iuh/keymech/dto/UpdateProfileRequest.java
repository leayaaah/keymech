package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.Email;

public record UpdateProfileRequest(
    String fullName,
    @Email String email,
    String phone,
    String address
) {}