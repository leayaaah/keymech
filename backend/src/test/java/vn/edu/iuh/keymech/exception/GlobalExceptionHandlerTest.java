package vn.edu.iuh.keymech.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("NotAuthenticatedException -> 401 UNAUTHORIZED, message 'Vui lòng đăng nhập'")
    void notAuthenticated_returns401() {
        var res = handler.notAuthenticated(new NotAuthenticatedException("Vui lòng đăng nhập"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().error()).isEqualTo("UNAUTHORIZED");
        assertThat(res.getBody().message()).isEqualTo("Vui lòng đăng nhập");
    }

    @Test
    @DisplayName("InvalidCredentialsException -> 401 INVALID_CREDENTIALS")
    void invalidCredentials_returns401() {
        var res = handler.invalidCredentials(new InvalidCredentialsException("x"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().error()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(res.getBody().message()).isEqualTo("Email hoặc mật khẩu không đúng");
    }

    @Test
    @DisplayName("InvalidPasswordException -> 400 INVALID_PASSWORD")
    void invalidPassword_returns400() {
        var res = handler.invalidPassword(new InvalidPasswordException(
            "INVALID_PASSWORD", "Mật khẩu cũ không đúng"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().error()).isEqualTo("INVALID_PASSWORD");
        assertThat(res.getBody().message()).isEqualTo("Mật khẩu cũ không đúng");
    }

    @Test
    @DisplayName("InvalidCodeException -> 400 INVALID_CODE")
    void invalidCode_returns400() {
        var res = handler.invalidCode(new InvalidCodeException("INVALID_CODE", "Mã xác thực không đúng"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().error()).isEqualTo("INVALID_CODE");
        assertThat(res.getBody().message()).isEqualTo("Mã xác thực không đúng");
    }

    @Test
    @DisplayName("BusinessException -> 409 (không đổi)")
    void business_returns409() {
        var res = handler.business(new vn.edu.iuh.keymech.exception.BusinessException(
            "FORBIDDEN", "Đã đăng nhập"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().error()).isEqualTo("FORBIDDEN");
    }
}
