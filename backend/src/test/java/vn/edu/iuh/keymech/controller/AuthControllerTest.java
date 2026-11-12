package vn.edu.iuh.keymech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.iuh.keymech.dto.*;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.AuthService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;

    @Test
    @DisplayName("TC-REG-01: POST /api/auth/register - thành công 201")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123", "0909", "Address");
        UserResponse response = new UserResponse(1L, "Test User", "test@example.com", "0909", "Address", "CUSTOMER", true);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("TC-REG-02: POST /api/auth/register - email trùng 409")
    void register_emailExists() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123", null, null);
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new vn.edu.iuh.keymech.exception.BusinessException("EMAIL_EXISTS", "Email đã được đăng ký"));

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("EMAIL_EXISTS"));
    }

    @Test
    @DisplayName("TC-SEC-18: POST /api/auth/register - đã đăng nhập 403")
    void register_alreadyAuthenticated() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123", null, null);
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new vn.edu.iuh.keymech.exception.BusinessException("FORBIDDEN", "Đã đăng nhập, không thể đăng ký"));

        // Mock authenticated user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .principal(() -> "customer@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - thành công 200")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("customer@example.com", "password");
        UserResponse response = new UserResponse(1L, "Customer", "customer@example.com", "0909", "Address", "CUSTOMER", true);

        when(authService.login(any(LoginRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("customer@example.com"));
    }

    @Test
    @DisplayName("GET /api/auth/me - trả về thông tin user")
    void me_success() throws Exception {
        UserResponse response = new UserResponse(1L, "Customer", "customer@example.com", "0909", "Address", "CUSTOMER", true);

        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
                .principal(() -> "customer@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("customer@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - luôn trả 200")
    void forgotPassword_alwaysReturnsOk() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Nếu email tồn tại và tài khoản đang hoạt động, mã xác thực đã được gửi"));

        verify(authService).forgotPassword(any(ForgotPasswordRequest.class), any());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - thành công")
    void resetPassword_success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newPass123");

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Đặt lại mật khẩu thành công"));

        verify(authService).resetPassword(any(ResetPasswordRequest.class), any());
    }

    @Test
    @DisplayName("TC-PWD-01: PUT /api/users/me/password - mật khẩu cũ sai 400")
    void changePassword_wrongOldPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongOld", "newPass123");
        when(authService.changePassword(any(ChangePasswordRequest.class), any(Authentication.class)))
            .thenThrow(new vn.edu.iuh.keymech.exception.BusinessException("INVALID_PASSWORD", "Mật khẩu cũ không đúng"));

        mockMvc.perform(put("/api/users/me/password")
                .with(csrf())
                .principal(() -> "customer@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_PASSWORD"));
    }

    @Test
    @DisplayName("PUT /api/users/me/password - thành công")
    void changePassword_success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("correctOld", "newPass123");

        mockMvc.perform(put("/api/users/me/password")
                .with(csrf())
                .principal(() -> "customer@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Đổi mật khẩu thành công"));

        verify(authService).changePassword(any(ChangePasswordRequest.class), any(Authentication.class));
    }

    @Test
    @DisplayName("POST /api/auth/logout - thành công")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .principal(() -> "customer@example.com"))
            .andExpect(status().isOk());

        verify(authService).logout(any());
    }
}