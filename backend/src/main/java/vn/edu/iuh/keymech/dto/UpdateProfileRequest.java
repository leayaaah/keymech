package vn.edu.iuh.keymech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    String fullName,
    @Email String email,
    String phone,
    String address
) {}