package vn.edu.iuh.keymech.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.ChangePasswordRequest;
import vn.edu.iuh.keymech.dto.ForgotPasswordRequest;
import vn.edu.iuh.keymech.dto.LoginRequest;
import vn.edu.iuh.keymech.dto.RegisterRequest;
import vn.edu.iuh.keymech.dto.ResetPasswordRequest;
import vn.edu.iuh.keymech.dto.UpdateProfileRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.InvalidCodeException;
import vn.edu.iuh.keymech.exception.InvalidCredentialsException;
import vn.edu.iuh.keymech.exception.InvalidPasswordException;
import vn.edu.iuh.keymech.exception.NotAuthenticatedException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserRepository;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.util.CodeGenerator;
import vn.edu.iuh.keymech.util.SessionKeys;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AuthService {
    private static final int MAX_RESET_REQUESTS_PER_WINDOW = 5;
    private static final long RESET_REQUEST_WINDOW_MILLIS = 60 * 60 * 1000L;
    private static final long RESET_REQUEST_INTERVAL_MILLIS = 60 * 1000L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final EmailService emailService;
    private final CodeGenerator codeGenerator;
    private Clock clock = Clock.systemUTC();

    // Bộ giới hạn yêu cầu đặt lại mật khẩu trong bộ nhớ: email -> trạng thái cửa sổ
    private final Map<String, ResetRequestWindow> resetRequestWindows = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository,
                       EmailService emailService,
                       CodeGenerator codeGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.emailService = emailService;
        this.codeGenerator = codeGenerator;
    }

    public UserResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Xác thực; mọi lỗi xác thực đều trả về INVALID_CREDENTIALS (401)
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Email hoặc mật khẩu không đúng");
        }

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        // Đổi mã phiên để ngăn chiếm quyền phiên mà không vô hiệu hóa phiên cũ
        HttpSession session = httpRequest.getSession(true);
        httpRequest.changeSessionId();

        // Nhân viên/Admin xóa giỏ; khách hàng giữ nguyên giỏ trong phiên.
        if (userDetails.getRole().equals("STAFF") || userDetails.getRole().equals("ADMIN")) {
            session.removeAttribute(SessionKeys.CART);
        }

        // Lưu ngữ cảnh bảo mật
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        securityContextRepository.saveContext(securityContext, httpRequest, null);

        return toResponse(user);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("EMAIL_EXISTS", "Email đã được đăng ký");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        User saved = userRepository.save(user);

        // Gửi email chào mừng bất đồng bộ
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName());

        return toResponse(saved);
    }

    public UserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new NotAuthenticatedException("Vui lòng đăng nhập");
        }
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        return toResponse(user);
    }

    public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.email();
        long now = clock.millis();
        removeExpiredResetRequests(now);
        User user = userRepository.findByEmail(email).orElse(null);

        // Luôn trả cùng thông báo để tránh dò tìm email
        if (user != null && user.isEnabled()) {
            // Giới hạn tối đa 5 yêu cầu mỗi giờ cho mỗi email
            if (!registerResetRequest(email, now)) {
                return;
            }

            // Tạo mã gồm 6 chữ số
            String code = codeGenerator.resetCode();
            String codeHash = hashResetCode(code);

            // Lưu vào phiên
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(SessionKeys.RESET_CODE + "_" + email, codeHash);
            session.setAttribute(SessionKeys.RESET_CODE + "_" + email + "_expires",
                LocalDateTime.now().plusMinutes(10));
            session.setAttribute(SessionKeys.RESET_CODE + "_" + email + "_attempts", 0);

            // Gửi email bất đồng bộ
            emailService.sendResetPasswordEmail(user.getEmail(), code);
        }
        // Luôn trả về mà không tiết lộ email có tồn tại hay không
    }

    public void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.email();
        String code = request.code();

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            throw new InvalidCodeException("INVALID_CODE", "Phiên làm việc đã hết hạn, vui lòng yêu cầu mã mới");
        }

        String storedHash = (String) session.getAttribute(SessionKeys.RESET_CODE + "_" + email);
        LocalDateTime expires = (LocalDateTime) session.getAttribute(SessionKeys.RESET_CODE + "_" + email + "_expires");
        Integer attempts = (Integer) session.getAttribute(SessionKeys.RESET_CODE + "_" + email + "_attempts");

        if (storedHash == null || expires == null) {
            throw new InvalidCodeException("INVALID_CODE", "Mã xác thực không hợp lệ hoặc đã hết hạn");
        }

        if (LocalDateTime.now().isAfter(expires)) {
            clearResetCode(session, email);
            throw new InvalidCodeException("INVALID_CODE", "Mã xác thực đã hết hạn");
        }

        if (attempts != null && attempts >= 5) {
            clearResetCode(session, email);
            throw new InvalidCodeException("INVALID_CODE", "Nhập sai quá nhiều lần, vui lòng yêu cầu mã mới");
        }

        byte[] codeHash = hashResetCode(code).getBytes(StandardCharsets.UTF_8);
        byte[] storedHashBytes = storedHash.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(codeHash, storedHashBytes)) {
            attempts = (attempts == null ? 1 : attempts + 1);
            session.setAttribute(SessionKeys.RESET_CODE + "_" + email + "_attempts", attempts);
            throw new InvalidCodeException("INVALID_CODE", "Mã xác thực không đúng");
        }

        // Mã hợp lệ: cập nhật mật khẩu
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Xóa mã đặt lại khỏi phiên
        clearResetCode(session, email);
    }

    public void changePassword(ChangePasswordRequest request, Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("INVALID_PASSWORD", "Mật khẩu cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public UserResponse updateProfile(UpdateProfileRequest request, Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        // Không cho đổi email trùng
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException("EMAIL_EXISTS", "Email đã được sử dụng");
            }
            user.setEmail(request.email());
        }

        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.address() != null) user.setAddress(request.address());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private void clearResetCode(HttpSession session, String email) {
        session.removeAttribute(SessionKeys.RESET_CODE + "_" + email);
        session.removeAttribute(SessionKeys.RESET_CODE + "_" + email + "_expires");
        session.removeAttribute(SessionKeys.RESET_CODE + "_" + email + "_attempts");
    }

    private synchronized boolean registerResetRequest(String email, long now) {
        ResetRequestWindow window = resetRequestWindows.get(email);
        if (window == null) {
            resetRequestWindows.put(email, new ResetRequestWindow(1, now, now));
            return true;
        }

        if (window.requestCount() >= MAX_RESET_REQUESTS_PER_WINDOW
                || now - window.lastRequestAt() < RESET_REQUEST_INTERVAL_MILLIS) {
            return false;
        }

        resetRequestWindows.put(email,
            new ResetRequestWindow(window.requestCount() + 1, window.windowStartedAt(), now));
        return true;
    }

    private synchronized void removeExpiredResetRequests(long now) {
        resetRequestWindows.entrySet().removeIf(entry ->
            now - entry.getValue().windowStartedAt() >= RESET_REQUEST_WINDOW_MILLIS);
    }

    private record ResetRequestWindow(int requestCount, long windowStartedAt, long lastRequestAt) {}

    private String hashResetCode(String code) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            user.getAddress(),
            user.getRole().name(),
            user.isEnabled()
        );
    }
}
