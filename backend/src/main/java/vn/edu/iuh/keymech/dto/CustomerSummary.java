package vn.edu.iuh.keymech.dto;

/** Thông tin khách hiển thị cho Staff, chỉ có trong phản hồi của /api/staff/**. */
public record CustomerSummary(Long id, String fullName, String email, String phone) {}
