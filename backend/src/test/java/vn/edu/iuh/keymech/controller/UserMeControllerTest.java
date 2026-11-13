package vn.edu.iuh.keymech.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.edu.iuh.keymech.dto.ChangePasswordRequest;
import vn.edu.iuh.keymech.dto.MessageResponse;
import vn.edu.iuh.keymech.dto.UpdateProfileRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.exception.InvalidPasswordException;
import vn.edu.iuh.keymech.exception.NotAuthenticatedException;
import vn.edu.iuh.keymech.service.AuthService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserMeControllerTest {

    @Test
    @DisplayName("TC-PWD-01: PUT /api/users/me/password - mật khẩu cũ sai 400 INVALID_PASSWORD")
    void changePassword_wrongOldPassword() {
        AuthService svc = mock(AuthService.class);
        UserMeController c = new UserMeController(svc);
        doThrow(new InvalidPasswordException("INVALID_PASSWORD", "Mật khẩu cũ không đúng"))
            .when(svc).changePassword(eq(new ChangePasswordRequest("wrongOld", "newPass123")), isNull());

        try {
            c.changePassword(new ChangePasswordRequest("wrongOld", "newPass123"), null);
        } catch (InvalidPasswordException e) {
            assertThat(e.getCode()).isEqualTo("INVALID_PASSWORD");
            assertThat(e.getMessage()).isEqualTo("Mật khẩu cũ không đúng");
            return;
        }
        throw new AssertionError("Expected InvalidPasswordException");
    }

    @Test
    @DisplayName("PUT /api/users/me/password - thành công")
    void changePassword_success() {
        AuthService svc = mock(AuthService.class);
        UserMeController c = new UserMeController(svc);

        ResponseEntity<MessageResponse> res = c.changePassword(new ChangePasswordRequest("old", "newPass123"), null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(svc).changePassword(any(ChangePasswordRequest.class), isNull());
    }

    @Test
    @DisplayName("PUT /api/users/me/password - Guest 401 UNAUTHORIZED")
    void changePassword_guest_401() {
        AuthService svc = mock(AuthService.class);
        UserMeController c = new UserMeController(svc);
        doThrow(new NotAuthenticatedException("Vui lòng đăng nhập"))
            .when(svc).changePassword(any(ChangePasswordRequest.class), isNull());

        try {
            c.changePassword(new ChangePasswordRequest("old", "newPass123"), null);
        } catch (NotAuthenticatedException e) {
            assertThat(e.getMessage()).isEqualTo("Vui lòng đăng nhập");
            return;
        }
        throw new AssertionError("Expected NotAuthenticatedException");
    }

    @Test
    @DisplayName("PUT /api/users/me - cập nhật hồ sơ thành công")
    void updateProfile_success() {
        AuthService svc = mock(AuthService.class);
        UserMeController c = new UserMeController(svc);
        UserResponse resp = new UserResponse(1L, "Nguyễn Văn B", "test@example.com", "0912", "Address", "CUSTOMER", true);
        when(svc.updateProfile(any(UpdateProfileRequest.class), any(Authentication.class))).thenReturn(resp);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "test@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        ResponseEntity<UserResponse> res = c.updateProfile(
            new UpdateProfileRequest("Nguyễn Văn B", "test@example.com", "0912", "Address"), auth);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().fullName()).isEqualTo("Nguyễn Văn B");
        assertThat(res.getBody().email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("PUT /api/users/me - Guest 401 UNAUTHORIZED")
    void updateProfile_guest_401() {
        AuthService svc = mock(AuthService.class);
        UserMeController c = new UserMeController(svc);
        doThrow(new NotAuthenticatedException("Vui lòng đăng nhập"))
            .when(svc).updateProfile(any(UpdateProfileRequest.class), isNull());

        try {
            c.updateProfile(new UpdateProfileRequest("Nguyễn Văn B", "test@example.com", "0912", "Address"), null);
        } catch (NotAuthenticatedException e) {
            assertThat(e.getMessage()).isEqualTo("Vui lòng đăng nhập");
            return;
        }
        throw new AssertionError("Expected NotAuthenticatedException");
    }
}
