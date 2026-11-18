package vn.edu.iuh.keymech.dto;

public record UserResponse(
    Long id,
    String fullName,
    String email,
    String phone,
    String address,
    String role,
    boolean enabled
) {}