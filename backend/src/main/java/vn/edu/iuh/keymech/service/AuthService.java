package vn.edu.iuh.keymech.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserRepository;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.util.CodeGenerator;
import vn.edu.iuh.keymech.util.SessionKeys;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final EmailService emailService;
    private final CodeGenerator codeGenerator;

    // In-memory rate limiter for forgot password: email -> count
    private final Map<String, Integer> resetRequestCount = new ConcurrentHashMap<>();
    private final Map<String, Long> lastResetRequestTime = new ConcurrentHashMap<>();

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
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        // Change session ID to prevent session fixation
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        HttpSession newSession = httpRequest.getSession(true);
        httpRequest.changeSessionId();

        // If Staff or Admin, remove cart
        if (userDetails.getRole().equals("STAFF") || userDetails.getRole().equals("ADMIN")) {
            newSession.removeAttribute(SessionKeys.CART);
        }

        // Save SecurityContext
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

        // Send welcome email asynchronously
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName());

        return toResponse(saved);
    }

    public UserResponse getCurrentUser(Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        return toResponse(user);
    }

    public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.email();
        User user = userRepository.findByEmail(email).orElse(null);

        // Always return same message to prevent email enumeration
        String message = "Nếu email tồn tại và tài khoản đang hoạt động, mã xác thực đã được gửi";

        if (user != null && user.isEnabled()) {
            // Rate limiting: max 5 requests per hour per email
            long now = System.currentTimeMillis();
            Long lastTime = lastResetRequestTime.get(email);
            if (lastTime != null && now - lastTime < 60_000) { // 60 seconds
                // Silently ignore, still return same message
                return;
            }

            Integer count = resetRequestCount.getOrDefault(email, 0);
            if (count >= 5) {
                // Silently ignore
                return;
            }

            // Generate 6-digit code
            String code = codeGenerator.resetCode();
            String codeHash = hashCode(code);

            // Store in session
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(SessionKeys.RESET_CODE + "_" + email, codeHash);
            session.setAttribute(SessionKeys.RESET_CODE + "_" + email + "_expires", LocalDateTime.now().plusMinutes(10));
            session.setAttribute(SessionKeys.RESET_CODE + "_" + email + "_attempts", 0);

            // Update rate limiter
            resetRequestCount.put(email, count + 1);
            lastResetRequestTime.put(email, now);

            // Send email asynchronously
            emailService.sendResetPasswordEmail(user.getEmail(), code);
        }
        // Always return without revealing whether email exists
    }

    public void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String email = request.email();
        String code = request.code();

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            throw new BusinessException("INVALID_CODE", "Phiên làm việc đã hết hạn, vui lòng yêu cầu mã mới");
        }

        String storedHash = (String) session.getAttribute(SessionKeys.RESET_CODE + "_" + email);
        LocalDateTime expires = (LocalDateTime) session.getAttribute(SessionKeys.RESET_CODE + "_" + email + "_expires");
        Integer attempts = (Integer) session.getAttribute(SessionKeys.RESET_CODE + "_" + email + "_attempts");

        if (storedHash == null || expires == null) {
            throw new BusinessException("INVALID_CODE", "Mã xác thực không hợp lệ hoặc đã hết hạn");
        }

        if (LocalDateTime.now().isAfter(expires)) {
            clearResetCode(session, email);
            throw new BusinessException("INVALID_CODE", "Mã xác thực đã hết hạn");
        }

        if (attempts != null && attempts >= 5) {
            clearResetCode(session, email);
            throw new BusinessException("INVALID_CODE", "Nhập sai quá nhiều lần, vui lòng yêu cầu mã mới");
        }

        if (!hashCode(code).equals(storedHash)) {
            attempts = (attempts == null ? 1 : attempts + 1);
            session.setAttribute(SessionKeys.RESET_CODE + "_" + email + "_attempts", attempts);
            throw new BusinessException("INVALID_CODE", "Mã xác thực không đúng");
        }

        // Valid code - update password
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Clear reset code from session
        clearResetCode(session, email);
    }

    public void changePassword(ChangePasswordRequest request, Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "Mật khẩu cũ không đúng");
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

    private String hashCode(String code) {
        // Simple hash for session storage - in production use proper password hashing
        return Base64.getEncoder().encodeToString(code.getBytes());
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