package vn.edu.iuh.keymech.service;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import vn.edu.iuh.keymech.dto.*;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserRepository;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.util.CodeGenerator;

import java.time.LocalDateTime;
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
    @Mock CodeGenerator codeGenerator;
    @Mock HttpServletRequest httpRequest;
    @Mock HttpSession session;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        when(httpRequest.getSession(false)).thenReturn(null);
        when(httpRequest.getSession(true)).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(null);
        doNothing().when(session).setAttribute(anyString(), any());
        doNothing().when(session).removeAttribute(anyString());
        when(session.getId()).thenReturn("test-session");
        when(httpRequest.changeSessionId()).thenReturn("new-session-id");
    }

    @Nested
    @DisplayName("TC-REG: Đăng ký")
    class RegisterTests {

        @Test
        @DisplayName("TC-REG-01: Đăng ký thành công - tạo user CUSTOMER, gửi email chào mừng")
        void register_success() {
            // Given
            RegisterRequest request = new RegisterRequest("Nguyễn Văn A", "test@example.com", "password123", "0909123456", "Hà Nội");
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            // When
            UserResponse response = authService.register(request);

            // Then
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
            // Given
            RegisterRequest request = new RegisterRequest("Nguyễn Văn A", "test@example.com", "password123", null, null);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "EMAIL_EXISTS");
        }
    }

    @Nested
    @DisplayName("TC-SEC-07, TC-SEC-19: Đăng nhập thủ công")
    class LoginTests {

        @Test
        @DisplayName("TC-SEC-07: Guest có giỏ, đăng nhập Customer - đổi session id, giữ giỏ")
        void login_customer_keepsCart_changesSessionId() {
            // Given
            LoginRequest request = new LoginRequest("customer@example.com", "password");
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
            when(httpRequest.getSession(false)).thenReturn(session);
            // Session has cart attribute
            when(session.getAttribute("cart")).thenReturn(java.util.Map.of(1L, 2));

            // When
            UserResponse response = authService.login(request, httpRequest);

            // Then
            assertThat(response.role()).isEqualTo("CUSTOMER");
            verify(httpRequest).changeSessionId();
            verify(session, never()).removeAttribute("cart"); // Cart should be kept for CUSTOMER
            verify(securityContextRepository).saveContext(any(SecurityContext.class), eq(httpRequest), any());
        }

        @Test
        @DisplayName("TC-SEC-19: Guest có giỏ, đăng nhập Staff - đổi session id, xóa giỏ")
        void login_staff_removesCart_changesSessionId() {
            // Given
            LoginRequest request = new LoginRequest("staff@example.com", "password");
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
            when(httpRequest.getSession(false)).thenReturn(session);
            when(session.getAttribute("cart")).thenReturn(java.util.Map.of(1L, 2));

            // When
            UserResponse response = authService.login(request, httpRequest);

            // Then
            assertThat(response.role()).isEqualTo("STAFF");
            verify(httpRequest).changeSessionId();
            verify(session).removeAttribute("cart"); // Cart should be removed for STAFF
        }

        @Test
        @DisplayName("TC-SEC-19 variant: Admin đăng nhập - xóa giỏ")
        void login_admin_removesCart() {
            // Given
            LoginRequest request = new LoginRequest("admin@example.com", "password");
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
            when(httpRequest.getSession(false)).thenReturn(session);
            when(session.getAttribute("cart")).thenReturn(java.util.Map.of(1L, 2));

            // When
            UserResponse response = authService.login(request, httpRequest);

            // Then
            assertThat(response.role()).isEqualTo("ADMIN");
            verify(session).removeAttribute("cart");
        }
    }

    @Nested
    @DisplayName("TC-PWD: Đổi mật khẩu")
    class ChangePasswordTests {

        @Test
        @DisplayName("TC-PWD-01: Đổi mật khẩu với mật khẩu cũ sai - ném BusinessException INVALID_PASSWORD")
        void changePassword_wrongOldPassword_throwsException() {
            // Given
            ChangePasswordRequest request = new ChangePasswordRequest("wrongOld", "newPassword123");
            User user = new User();
            user.setId(1L);
            user.setPassword("$2a$10$encoded");

            AppUserDetails userDetails = new AppUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongOld", "$2a$10$encoded")).thenReturn(false);

            // When / Then
            assertThatThrownBy(() -> authService.changePassword(request, auth))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_PASSWORD");
        }

        @Test
        @DisplayName("TC-PWD-02: Đổi mật khẩu thành công - cập nhật mật khẩu mới")
        void changePassword_success() {
            // Given
            ChangePasswordRequest request = new ChangePasswordRequest("correctOld", "newPassword123");
            User user = new User();
            user.setId(1L);
            user.setPassword("$2a$10$encoded");

            AppUserDetails userDetails = new AppUserDetails(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("correctOld", "$2a$10$encoded")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncoded");

            // When
            authService.changePassword(request, auth);

            // Then
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
            // Given
            ForgotPasswordRequest request = new ForgotPasswordRequest("notexist@example.com");
            when(userRepository.findByEmail("notexist@example.com")).thenReturn(Optional.empty());

            // When - không ném exception
            authService.forgotPassword(request, httpRequest);

            // Then
            verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("TC-PWD-04: Quên mật khẩu tài khoản bị ẩn - không gửi email")
        void forgotPassword_disabledAccount_noEmail() {
            // Given
            ForgotPasswordRequest request = new ForgotPasswordRequest("disabled@example.com");
            User user = new User();
            user.setEmail("disabled@example.com");
            user.setEnabled(false); // Tài khoản bị ẩn

            when(userRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(user));

            // When
            authService.forgotPassword(request, httpRequest);

            // Then
            verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("TC-PWD-02: Quên mật khẩu email hợp lệ - gửi mã, lưu session")
        void forgotPassword_validEmail_sendsCode() {
            // Given
            ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
            User user = new User();
            user.setId(1L);
            user.setEmail("test@example.com");
            user.setEnabled(true);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(codeGenerator.resetCode()).thenReturn("123456");

            // When
            authService.forgotPassword(request, httpRequest);

            // Then
            verify(session).setAttribute(eq("resetCode_test@example.com"), anyString());
            verify(session).setAttribute(eq("resetCode_test@example.com_expires"), any(LocalDateTime.class));
            verify(session).setAttribute(eq("resetCode_test@example.com_attempts"), eq(0));
            verify(emailService).sendResetPasswordEmail("test@example.com", "123456");
        }
    }

    @Nested
    @DisplayName("TC-PWD-02, TC-PWD-03: Reset mật khẩu")
    class ResetPasswordTests {

        @Test
        @DisplayName("TC-PWD-03: Reset mật khẩu mã hết hạn - ném BusinessException INVALID_CODE")
        void resetPassword_expiredCode_throwsException() {
            // Given
            ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newPass123");
            when(httpRequest.getSession(false)).thenReturn(session);
            when(session.getAttribute("resetCode_test@example.com")).thenReturn("hash");
            when(session.getAttribute("resetCode_test@example.com_expires")).thenReturn(LocalDateTime.now().minusMinutes(1));

            // When / Then
            assertThatThrownBy(() -> authService.resetPassword(request, httpRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_CODE");
        }

        @Test
        @DisplayName("TC-PWD-02: Reset mật khẩu nhập sai 5 lần - mã bị hủy")
        void resetPassword_maxAttempts_clearsCode() {
            // Given
            ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "wrong", "newPass123");
            when(httpRequest.getSession(false)).thenReturn(session);
            when(session.getAttribute("resetCode_test@example.com")).thenReturn("hash");
            when(session.getAttribute("resetCode_test@example.com_expires")).thenReturn(LocalDateTime.now().plusMinutes(5));
            when(session.getAttribute("resetCode_test@example.com_attempts")).thenReturn(5);

            // When / Then
            assertThatThrownBy(() -> authService.resetPassword(request, httpRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_CODE");

            verify(session).removeAttribute("resetCode_test@example.com");
            verify(session).removeAttribute("resetCode_test@example.com_expires");
            verify(session).removeAttribute("resetCode_test@example.com_attempts");
        }

        @Test
        @DisplayName("TC-PWD-02: Reset mật khẩu thành công - cập nhật mật khẩu, xóa mã")
        void resetPassword_success() {
            // Given
            ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newPass123");
            User user = new User();
            user.setId(1L);
            user.setEmail("test@example.com");
            user.setPassword("$2a$10$old");

            when(httpRequest.getSession(false)).thenReturn(session);
            when(session.getAttribute("resetCode_test@example.com")).thenReturn("MTIzNDU2"); // Base64 of "123456"
            when(session.getAttribute("resetCode_test@example.com_expires")).thenReturn(LocalDateTime.now().plusMinutes(5));
            when(session.getAttribute("resetCode_test@example.com_attempts")).thenReturn(0);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$new");

            // When
            authService.resetPassword(request, httpRequest);

            // Then
            verify(userRepository).save(user);
            assertThat(user.getPassword()).isEqualTo("$2a$10$new");
            verify(session).removeAttribute("resetCode_test@example.com");
        }
    }
}