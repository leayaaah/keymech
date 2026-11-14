package vn.edu.iuh.keymech.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.edu.iuh.keymech.dto.*;
import vn.edu.iuh.keymech.exception.InvalidCredentialsException;
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

class AuthControllerTest {

    @Test
    @DisplayName("TC-REG-01: POST /api/auth/register - thành công 201")
    void register_success() {
        AuthService svc = mock(AuthService.class);
        AuthController c = new AuthController(svc);
        RegisterRequest req = new RegisterRequest("T", "t@x.com", "password123", "0909", "A");
        UserResponse resp = new UserResponse(1L, "T", "t@x.com", "0909", "A", "CUSTOMER", true);
        when(svc.register(eq(req))).thenReturn(resp);

        ResponseEntity<UserResponse> res = c.register(req, null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody().email()).isEqualTo("t@x.com");
    }

    @Test
    @DisplayName("TC-REG-02: POST /api/auth/register - email trùng 409")
    void register_emailExists() {
        AuthService svc = mock(AuthService.class);
        AuthController c = new AuthController(svc);
        RegisterRequest req = new RegisterRequest("T", "t@x.com", "password123", null, null);
        when(svc.register(any(RegisterRequest.class)))
            .thenThrow(new vn.edu.iuh.keymech.exception.BusinessException(
                "EMAIL_EXISTS", "Email đã được đăng ký"));

        try {
            c.register(req, null);
        } catch (vn.edu.iuh.keymech.exception.BusinessException e) {
            assertThat(e.getCode()).isEqualTo("EMAIL_EXISTS");
            return;
        }
        throw new AssertionError("Expected BusinessException");
    }

    @Test
    @DisplayName("TC-SEC-18: POST /api/auth/register - đã đăng nhập 403")
    void register_alreadyAuthenticated() {
        AuthService svc = mock(AuthService.class);
        AuthController c = new AuthController(svc);
        doThrow(new org.springframework.security.access.AccessDeniedException("Đã đăng nhập"))
            .when(svc).register(any(RegisterRequest.class));
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "customer@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        try {
            c.register(new RegisterRequest("T", "t@x.com", "password123", null, null), auth);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return; // GlobalExceptionHandler maps AccessDeniedException -> 403 FORBIDDEN
        }
        throw new AssertionError("Expected AccessDeniedException");
    }

    @Test
    @DisplayName("POST /api/auth/login - thành công 200")
    void login_success() {
        AuthService svc = mock(AuthService.class);
        AuthController c = new AuthController(svc);
        UserResponse resp = new UserResponse(1L, "C", "c@x.com", "0909", "A", "CUSTOMER", true);
        when(svc.login(eq(new LoginRequest("c@x.com", "pw")), isNull())).thenReturn(resp);

        ResponseEntity<UserResponse> res = c.login(new LoginRequest("c@x.com", "pw"), null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().email()).isEqualTo("c@x.com");
    }

    @Test
    @DisplayName("POST /api/auth/login - sai mật khẩu 401 INVALID_CREDENTIALS")
    void login_invalidCredentials() {
        AuthService svc = mock(AuthService.class);
        AuthController c = new AuthController(svc);
        when(svc.login(eq(new LoginRequest("c@x.com", "wrong")), isNull()))
            .thenThrow(new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));

        try {
            c.login(new LoginRequest("c@x.com", "wrong"), null);
        } catch (InvalidCredentialsException e) {
            assertThat(e.getMessage()).isEqualTo("Email hoặc mật khẩu không đúng");
            return;
        }
        throw new AssertionError("Expected InvalidCredentialsException");
    }

    @Test
    @DisplayName("GET /api/auth/me - trả về thông tin user đã đăng nhập")
    void me_success() {
        AuthService svc = mock(AuthService.class);
        AuthController c = new AuthController(svc);
        UserResponse resp = new UserResponse(1L, "C", "c@x.com", "0909", "A", "CUSTOMER", true);
        when(svc.getCurrentUser(any(Authentication.class))).thenReturn(resp);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "c@x.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        ResponseEntity<UserResponse> res = c.me(auth);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().email()).isEqualTo("c@x.com");
    }

    @Test
    @DisplayName("GET /api/auth/me - Guest 401 UNAUTHORIZED")
    void me_guest_401() {
        AuthService svc = mock(AuthService.class);
        AuthController c = new AuthController(svc);
        when(svc.getCurrentUser(isNull()))
            .thenThrow(new vn.edu.iuh.keymech.exception.NotAuthenticatedException("Vui lòng đăng nhập"));

        try {
            c.me(null);
        } catch (vn.edu.iuh.keymech.exception.NotAuthenticatedException e) {
            assertThat(e.getMessage()).isEqualTo("Vui lòng đăng nhập");
            return;
        }
        throw new AssertionError("Expected NotAuthenticatedException");
    }

    @Test
    @DisplayName("POST /api/auth/logout - thành công")
    void logout_success() {
        AuthService svc = mock(AuthService.class);
        AuthController c = new AuthController(svc);

        ResponseEntity<Void> res = c.logout(null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(svc).logout(null);
    }
}
