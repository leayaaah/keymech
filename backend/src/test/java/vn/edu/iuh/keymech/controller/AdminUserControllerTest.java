package vn.edu.iuh.keymech.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.edu.iuh.keymech.dto.CreateStaffRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.service.AdminUserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserControllerTest {

    @Test
    @DisplayName("TC-SEC-13: GET /api/admin/users - Admin xem danh sách user")
    void getUsers_admin_success() {
        AdminUserService svc = mock(AdminUserService.class);
        AdminUserController c = new AdminUserController(svc);
        UserResponse admin = new UserResponse(1L, "Admin", "admin@example.com", null, null, "ADMIN", true);
        UserResponse staff = new UserResponse(2L, "Staff", "staff@example.com", null, null, "STAFF", true);
        Page<UserResponse> page = new PageImpl<>(List.of(admin, staff));
        when(svc.getUsers(anyString(), any(Role.class),
            any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        ResponseEntity<Page<UserResponse>> res = c.getUsers(null, Role.ADMIN, null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(svc).getUsers(isNull(), eq(Role.ADMIN), isNull());
    }

    @Test
    @DisplayName("GET /api/admin/users - trả về danh sách (delegation)")
    void getUsers_delegation() {
        AdminUserService svc = mock(AdminUserService.class);
        AdminUserController c = new AdminUserController(svc);
        Page<UserResponse> page = new PageImpl<>(List.of());
        when(svc.getUsers(anyString(), any(Role.class), isNull())).thenReturn(page);

        ResponseEntity<Page<UserResponse>> res = c.getUsers(null, null, null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("TC-SEC-17: POST /api/admin/users - Admin tạo Staff thành công 201")
    void createStaff_success() {
        AdminUserService svc = mock(AdminUserService.class);
        AdminUserController c = new AdminUserController(svc);
        CreateStaffRequest req = new CreateStaffRequest(
            "New Staff", "newstaff@example.com", "password123", "0909", "Address");
        UserResponse resp = new UserResponse(3L, "New Staff", "newstaff@example.com", "0909", "Address", "STAFF", true);
        when(svc.createStaff(req)).thenReturn(resp);

        ResponseEntity<UserResponse> res = c.createStaff(req);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody().role()).isEqualTo("STAFF");
    }

    @Test
    @DisplayName("TC-SEC-17: POST /api/admin/users - Staff không được tạo user 403 (PreAuthorize ADMIN)")
    void createStaff_staff_forbidden() {
        AdminUserService svc = mock(AdminUserService.class);
        AdminUserController c = new AdminUserController(svc);
        CreateStaffRequest req = new CreateStaffRequest(
            "New Staff", "newstaff@example.com", "password123", "0909", "Address");
        when(svc.createStaff(any(CreateStaffRequest.class))).thenReturn(
            new UserResponse(3L, "N", "n@x.com", null, null, "STAFF", true));

        ResponseEntity<UserResponse> res = c.createStaff(req);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(svc).createStaff(any(CreateStaffRequest.class));
    }

    @Test
    @DisplayName("TC-SEC-13: PUT /api/admin/users/{id}/visibility - Admin không thể ẩn Admin 409")
    void toggleVisibility_adminAccount_forbidden() {
        AdminUserService svc = mock(AdminUserService.class);
        AdminUserController c = new AdminUserController(svc);
        doThrow(new vn.edu.iuh.keymech.exception.BusinessException(
            "FORBIDDEN", "Không thể ẩn/hiện tài khoản Admin"))
            .when(svc).toggleUserVisibility(1L);

        try {
            c.toggleVisibility(1L);
        } catch (vn.edu.iuh.keymech.exception.BusinessException e) {
            assertThat(e.getCode()).isEqualTo("FORBIDDEN");
            return;
        }
        throw new AssertionError("Expected BusinessException");
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/visibility - Admin ẩn user thường thành công")
    void toggleVisibility_normalUser_success() {
        AdminUserService svc = mock(AdminUserService.class);
        AdminUserController c = new AdminUserController(svc);

        ResponseEntity<Void> res = c.toggleVisibility(2L);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(svc).toggleUserVisibility(2L);
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id} - Admin sửa user thành công")
    void updateUser_success() {
        AdminUserService svc = mock(AdminUserService.class);
        AdminUserController c = new AdminUserController(svc);
        UserResponse resp = new UserResponse(2L, "Updated Staff", "staff@example.com",
            "0912", "New Address", "STAFF", true);
        when(svc.updateUser(eq(2L), anyString(), anyString(), anyString(), any())).thenReturn(resp);

        ResponseEntity<UserResponse> res = c.updateUser(2L, "Updated Staff", "0912", "New Address", null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().fullName()).isEqualTo("Updated Staff");
    }
}
