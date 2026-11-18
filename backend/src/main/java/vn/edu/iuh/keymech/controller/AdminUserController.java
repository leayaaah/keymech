package vn.edu.iuh.keymech.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.keymech.dto.CreateStaffRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = adminUserService.getUsers(keyword, role, pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        UserResponse response = adminUserService.createStaff(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                    @RequestParam String fullName,
                                                    @RequestParam(required = false) String phone,
                                                    @RequestParam(required = false) String address,
                                                    Authentication authentication) {
        UserResponse response = adminUserService.updateUser(id, fullName, phone, address, authentication);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<Void> toggleVisibility(@PathVariable Long id) {
        adminUserService.toggleUserVisibility(id);
        return ResponseEntity.ok().build();
    }
}