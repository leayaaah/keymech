package vn.edu.iuh.keymech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.iuh.keymech.dto.CreateStaffRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.service.AdminUserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AdminUserService adminUserService;

    @Test
    @DisplayName("TC-SEC-13: GET /api/admin/users - Admin xem danh sách user")
    @WithMockUser(roles = "ADMIN")
    void getUsers_admin_success() throws Exception {
        UserResponse admin = new UserResponse(1L, "Admin", "admin@example.com", null, null, "ADMIN", true);
        UserResponse staff = new UserResponse(2L, "Staff", "staff@example.com", null, null, "STAFF", true);
        Page<UserResponse> page = new PageImpl<>(List.of(admin, staff));

        when(adminUserService.getUsers(anyString(), any(Role.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("TC-SEC-10: GET /api/admin/users - Staff truy cập bị 403")
    @WithMockUser(roles = "STAFF")
    void getUsers_staff_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-SEC-11: GET /api/admin/users - Admin truy cập Staff endpoint (nếu có) - nhưng đây là Admin endpoint")
    @WithMockUser(roles = "ADMIN")
    void getUsers_admin_ok() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of());
        when(adminUserService.getUsers(anyString(), any(Role.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-SEC-17: POST /api/admin/users - Admin tạo Staff thành công 201")
    @WithMockUser(roles = "ADMIN")
    void createStaff_success() throws Exception {
        CreateStaffRequest request = new CreateStaffRequest("New Staff", "newstaff@example.com", "password123", "0909", "Address");
        UserResponse response = new UserResponse(3L, "New Staff", "newstaff@example.com", "0909", "Address", "STAFF", true);

        when(adminUserService.createStaff(any(CreateStaffRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("STAFF"))
            .andExpect(jsonPath("$.email").value("newstaff@example.com"));
    }

    @Test
    @DisplayName("TC-SEC-17: POST /api/admin/users - Staff không được tạo user 403")
    @WithMockUser(roles = "STAFF")
    void createStaff_staff_forbidden() throws Exception {
        CreateStaffRequest request = new CreateStaffRequest("New Staff", "newstaff@example.com", "password123", "0909", "Address");

        mockMvc.perform(post("/api/admin/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-SEC-13: PUT /api/admin/users/{id}/visibility - Admin không thể ẩn Admin 409")
    @WithMockUser(roles = "ADMIN")
    void toggleVisibility_adminAccount_forbidden() throws Exception {
        doThrow(new vn.edu.iuh.keymech.exception.BusinessException("FORBIDDEN", "Không thể ẩn/hiện tài khoản Admin"))
            .when(adminUserService).toggleUserVisibility(1L);

        mockMvc.perform(put("/api/admin/users/1/visibility")
                .with(csrf()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/visibility - Admin ẩn user thường thành công")
    @WithMockUser(roles = "ADMIN")
    void toggleVisibility_normalUser_success() throws Exception {
        mockMvc.perform(put("/api/admin/users/2/visibility")
                .with(csrf()))
            .andExpect(status().isOk());

        verify(adminUserService).toggleUserVisibility(2L);
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id} - Admin sửa user thành công")
    @WithMockUser(roles = "ADMIN")
    void updateUser_success() throws Exception {
        UserResponse response = new UserResponse(2L, "Updated Staff", "staff@example.com", "0912", "New Address", "STAFF", true);
        when(adminUserService.updateUser(eq(2L), anyString(), anyString(), anyString(), any()))
            .thenReturn(response);

        mockMvc.perform(put("/api/admin/users/2")
                .with(csrf())
                .param("fullName", "Updated Staff")
                .param("phone", "0912")
                .param("address", "New Address"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Updated Staff"));
    }
}