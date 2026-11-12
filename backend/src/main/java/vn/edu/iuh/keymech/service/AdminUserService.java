package vn.edu.iuh.keymech.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.CreateStaffRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponse> getUsers(String keyword, Role role, Pageable pageable) {
        if (keyword != null && role != null) {
            return userRepository.findByFullNameContainingIgnoreCaseAndRole(keyword, role, pageable)
                .map(this::toResponse);
        } else if (keyword != null) {
            return userRepository.findByFullNameContainingIgnoreCase(keyword, pageable)
                .map(this::toResponse);
        } else if (role != null) {
            return userRepository.findByRole(role, pageable)
                .map(this::toResponse);
        }
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public UserResponse createStaff(CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("EMAIL_EXISTS", "Email đã được sử dụng");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRole(Role.STAFF); // Hardcoded to STAFF
        user.setEnabled(true);

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public UserResponse updateUser(Long id, String fullName, String phone, String address, Authentication authentication) {
        User target = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // Cannot modify ADMIN account
        if (target.getRole() == Role.ADMIN) {
            throw new BusinessException("FORBIDDEN", "Không thể sửa tài khoản Admin");
        }

        // Check if current user is trying to modify their own role (not allowed anyway)
        // Role is not in the update fields

        target.setFullName(fullName);
        target.setPhone(phone);
        target.setAddress(address);

        User saved = userRepository.save(target);
        return toResponse(saved);
    }

    public void toggleUserVisibility(Long id) {
        User target = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (target.getRole() == Role.ADMIN) {
            throw new BusinessException("FORBIDDEN", "Không thể ẩn/hiện tài khoản Admin");
        }

        target.setEnabled(!target.isEnabled());
        userRepository.save(target);
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