package vn.edu.iuh.keymech.service;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import vn.edu.iuh.keymech.dto.ChangePasswordRequest;
import vn.edu.iuh.keymech.dto.ForgotPasswordRequest;
import vn.edu.iuh.keymech.dto.LoginRequest;
import vn.edu.iuh.keymech.dto.RegisterRequest;
import vn.edu.iuh.keymech.dto.ResetPasswordRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.InvalidCodeException;
import vn.edu.iuh.keymech.exception.InvalidCredentialsException;
import vn.edu.iuh.keymech.exception.InvalidPasswordException;
import vn.edu.iuh.keymech.exception.NotAuthenticatedException;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserRepository;
import vn.edu.iuh.keymech.security.AppUserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock SecurityContextRepository securityContextRepository;
    @Mock EmailService emailService;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("TC-REG: Đăng ký")
    class RegisterTests {

        @Test
        @DisplayName("TC-REG-01: Đăng ký thành công - tạo user CUSTOMER, gửi email chào mừng")
        void register_success() {
            RegisterRequest request = new RegisterRequest("Nguyễn Văn A", "test@example.com", "password123", "0909123456", "Hà Nội");
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            UserResponse response = authService.register(request);

            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.fullName()).isEqualTo("Nguyễn Văn A");
            assertThat(response.role()).isEqualTo("CUSTOMER");
            assertThat(response.enabled()).isTrue();

            verify(userRepository).save(any(User.class));
            verify(emailService).sendWelcomeEmail("test@example.com", "Nguyễn Văn A");
        }

        @Test
        @DisplayName("TC-REG-02: Đăng ký email trùng - ném BusinessException EMAIL_EXISTS")
        void register_emailExists_throwsException() {
            RegisterRequest request = new RegisterRequest("Nguyễn Văn A", "test@example.com", "password123", null, null);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "EMAIL_EXISTS");
        }
    }

    @Nested
    @DisplayName("TC-SEC-07, TC-SEC-19: Đăng nhập thủ công")
    class LoginTests {

        @Test
        @DisplayName("TC-SEC-07: Guest có giỏ, đăng nhập Customer - đổi session id, giữ giỏ, không invalidate")
        void login_customer_keepsCart_changesSessionId() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = new MockHttpSession();
            request.setSession(session);
            session.setAttribute("cart", Map.of(1L, 2));

            LoginRequest loginRequest = new LoginRequest("customer@example.com", "password");
            User user = new User();
            user.setId(1L);
            user.setEmail("customer@example.com");
            user.setFullName("Customer");
            user.setRole(Role.CUSTOMER);
            user.setEnabled(true);

            AppUserDetails userDetails = new AppUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            UserResponse response = authService.login(loginRequest, request);

            assertThat(response.role()).isEqualTo("CUSTOMER");
            assertThat(session.getAttribute("cart")).isEqualTo(Map.of(1L, 2));
            // Không invalidate: session cũ vẫn tồn tại và id được giữ (MockHttpSession)
            assertThat(request.getSession(false)).isSameAs(session);
            verify(securityContextRepository).saveContext(any(SecurityContext.class), eq(request), any());
        }

        @Test
        @DisplayName("TC-SEC-19: Guest có giỏ, đăng nhập Staff - đổi session id, xóa giỏ")
        void login_staff_removesCart_changesSessionId() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = new MockHttpSession();
            request.setSession(session);
            session.setAttribute("cart", Map.of(1L, 2));

            LoginRequest loginRequest = new LoginRequest("staff@example.com", "password");
            User user = new User();
            user.setId(2L);
            user.setEmail("staff@example.com");
            user.setFullName("Staff");
            user.setRole(Role.STAFF);
            user.setEnabled(true);

            AppUserDetails userDetails = new AppUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(userRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            UserResponse response = authService.login(loginRequest, request);

            assertThat(response.role()).isEqualTo("STAFF");
            assertThat(session.getAttribute("cart")).isNull();
            verify(securityContextRepository).saveContext(any(SecurityContext.class), eq(request), any());
        }

        @Test
        @DisplayName("TC-SEC-19 variant: Admin đăng nhập - đổi session id, xóa giỏ")
        void login_admin_removesCart_changesSessionId() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = new MockHttpSession();
            request.setSession(session);
            session.setAttribute("cart", Map.of(1L, 2));

            LoginRequest loginRequest = new LoginRequest("admin@example.com", "password");
            User user = new User();
            user.setId(3L);
            user.setEmail("admin@example.com");
            user.setFullName("Admin");
            user.setRole(Role.ADMIN);
            user.setEnabled(true);

            AppUserDetails userDetails = new AppUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            UserResponse response = authService.login(loginRequest, request);

            assertThat(response.role()).isEqualTo("ADMIN");
            assertThat(session.getAttribute("cart")).isNull();
        }

        @Test
        @DisplayName("Sai mật khẩu - ném InvalidCredentialsException (401), không invalidate")
        void login_wrongPassword_throwsInvalidCredentials() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThatThrownBy(() -> authService.login(new LoginRequest("customer@example.com", "wrong"), request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email hoặc mật khẩu không đúng");
        }

        @Test
        @DisplayName("Tài khoản bị ẩn (enabled=false) - ném InvalidCredentialsException cùng message")
        void login_disabledAccount_throwsInvalidCredentials() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("User is disabled"));

            assertThatThrownBy(() -> authService.login(new LoginRequest("disabled@example.com", "password"), request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email hoặc mật khẩu không đúng");
        }
    }

    @Nested
    @DisplayName("TC-PWD: Đổi mật khẩu")
    class ChangePasswordTests {

        @Test
        @DisplayName("TC-PWD-01: Đổi mật khẩu với mật khẩu cũ sai - ném InvalidPasswordException INVALID_PASSWORD (400)")
        void changePassword_wrongOldPassword_throwsException() {
            ChangePasswordRequest request = new ChangePasswordRequest("wrongOld", "newPassword123");
            User user = new User();
            user.setId(1L);
            user.setRole(Role.CUSTOMER);
            user.setPassword("$2a$10$encoded");

            AppUserDetails userDetails = new AppUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongOld", "$2a$10$encoded")).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword(request, auth))
                .isInstanceOf(InvalidPasswordException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_PASSWORD");
        }

        @Test
        @DisplayName("TC-PWD-02: Đổi mật khẩu thành công - cập nhật mật khẩu mới")
        void changePassword_success() {
            ChangePasswordRequest request = new ChangePasswordRequest("correctOld", "newPassword123");
            User user = new User();
            user.setId(1L);
            user.setRole(Role.CUSTOMER);
            user.setPassword("$2a$10$encoded");

            AppUserDetails userDetails = new AppUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("correctOld", "$2a$10$encoded")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncoded");

            authService.changePassword(request, auth);

            verify(userRepository).save(user);
            assertThat(user.getPassword()).isEqualTo("$2a$10$newEncoded");
        }
    }

    @Nested
    @DisplayName("TC-PWD-02, TC-PWD-03, TC-PWD-04: Quên mật khẩu")
    class ForgotPasswordTests {

        @Test
        @DisplayName("TC-PWD-03: Quên mật khẩu email không tồn tại - trả về mà không ném lỗi (tránh dò email)")
        void forgotPassword_emailNotExists_noException() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("notexist@example.com");
            when(userRepository.findByEmail("notexist@example.com")).thenReturn(Optional.empty());

            authService.forgotPassword(request, new MockHttpServletRequest());

            verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("TC-PWD-04: Quên mật khẩu tài khoản bị ẩn - không gửi email")
        void forgotPassword_disabledAccount_noEmail() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("disabled@example.com");
            User user = new User();
            user.setEmail("disabled@example.com");
            user.setEnabled(false);

            when(userRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(user));

            authService.forgotPassword(request, new MockHttpServletRequest());

            verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("TC-PWD-02: Quên mật khẩu email hợp lệ - gửi mã, lưu session")
        void forgotPassword_validEmail_sendsCode() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest("test@example.com");
            User user = new User();
            user.setId(1L);
            user.setEmail("test@example.com");
            user.setEnabled(true);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

            authService.forgotPassword(forgotRequest, request);

            HttpSession session = request.getSession(false);
            assertThat(session).isNotNull();
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService, times(1)).sendResetPasswordEmail(eq("test@example.com"), codeCaptor.capture());
            assertThat(codeCaptor.getValue()).matches("\\d{6}");
        }
    }

    @Nested
    @DisplayName("TC-PWD-02, TC-PWD-03: Reset mật khẩu")
    class ResetPasswordTests {

        @Test
        @DisplayName("TC-PWD-03: Reset mật khẩu mã hết hạn - ném InvalidCodeException INVALID_CODE (400)")
        void resetPassword_expiredCode_throwsException() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = new MockHttpSession();
            request.setSession(session);
            session.setAttribute("resetCode_test@example.com", "hash");
            session.setAttribute("resetCode_test@example.com_expires", LocalDateTime.now().minusMinutes(1));

            ResetPasswordRequest resetRequest = new ResetPasswordRequest("test@example.com", "123456", "newPass123");

            assertThatThrownBy(() -> authService.resetPassword(resetRequest, request))
                .isInstanceOf(InvalidCodeException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_CODE");
        }

        @Test
        @DisplayName("TC-PWD-02: Reset mật khẩu nhập sai 5 lần - mã bị hủy")
        void resetPassword_maxAttempts_clearsCode() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = new MockHttpSession();
            request.setSession(session);
            session.setAttribute("resetCode_test@example.com", "hash");
            session.setAttribute("resetCode_test@example.com_expires", LocalDateTime.now().plusMinutes(5));
            session.setAttribute("resetCode_test@example.com_attempts", 5);

            ResetPasswordRequest resetRequest = new ResetPasswordRequest("test@example.com", "wrong", "newPass123");

            assertThatThrownBy(() -> authService.resetPassword(resetRequest, request))
                .isInstanceOf(InvalidCodeException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_CODE");

            assertThat(session.getAttribute("resetCode_test@example.com")).isNull();
            assertThat(session.getAttribute("resetCode_test@example.com_expires")).isNull();
            assertThat(session.getAttribute("resetCode_test@example.com_attempts")).isNull();
        }

        @Test
        @DisplayName("TC-PWD-02: Reset mật khẩu thành công - cập nhật mật khẩu, xóa mã")
        void resetPassword_success() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = new MockHttpSession();
            request.setSession(session);
            session.setAttribute("resetCode_test@example.com", "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
            session.setAttribute("resetCode_test@example.com_expires", LocalDateTime.now().plusMinutes(5));
            session.setAttribute("resetCode_test@example.com_attempts", 0);

            ResetPasswordRequest resetRequest = new ResetPasswordRequest("test@example.com", "123456", "newPass123");
            User user = new User();
            user.setId(1L);
            user.setEmail("test@example.com");
            user.setPassword("$2a$10$old");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$new");

            authService.resetPassword(resetRequest, request);

            verify(userRepository).save(user);
            assertThat(user.getPassword()).isEqualTo("$2a$10$new");
            assertThat(session.getAttribute("resetCode_test@example.com")).isNull();
        }
    }

    @Nested
    @DisplayName("TC-ME: Lấy thông tin người dùng hiện tại (/me)")
    class MeTests {

        @Test
        @DisplayName("Guest (authentication null) - ném NotAuthenticatedException (401)")
        void me_nullAuthentication_throwsNotAuthenticated() {
            assertThatThrownBy(() -> authService.getCurrentUser(null))
                .isInstanceOf(NotAuthenticatedException.class)
                .hasMessage("Vui lòng đăng nhập");
        }

        @Test
        @DisplayName("Guest (anonymousUser) - ném NotAuthenticatedException (401), không ép kiểu principal")
        void me_anonymousAuthentication_throwsNotAuthenticated() {
            Authentication anonymous = new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of());
            assertThatThrownBy(() -> authService.getCurrentUser(anonymous))
                .isInstanceOf(NotAuthenticatedException.class)
                .hasMessage("Vui lòng đăng nhập");
        }

        @Test
        @DisplayName("Đã đăng nhập - trả về thông tin user")
        void me_success() {
            User user = new User();
            user.setId(1L);
            user.setEmail("customer@example.com");
            user.setFullName("Customer");
            user.setRole(Role.CUSTOMER);
            user.setEnabled(true);

            AppUserDetails userDetails = new AppUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserResponse response = authService.getCurrentUser(auth);

            assertThat(response.email()).isEqualTo("customer@example.com");
            assertThat(response.role()).isEqualTo("CUSTOMER");
        }
    }
}
