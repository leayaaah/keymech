package vn.edu.iuh.keymech.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.iuh.keymech.dto.CreateStaffRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AdminUserService adminUserService;

    User adminUser;
    User staffUser;
    User customerUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFullName("Admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setEnabled(true);

        staffUser = new User();
        staffUser.setId(2L);
        staffUser.setEmail("staff@example.com");
        staffUser.setFullName("Staff");
        staffUser.setRole(Role.STAFF);
        staffUser.setEnabled(true);

        customerUser = new User();
        customerUser.setId(3L);
        customerUser.setEmail("customer@example.com");
        customerUser.setFullName("Customer");
        customerUser.setRole(Role.CUSTOMER);
        customerUser.setEnabled(true);
    }

    @Nested
    @DisplayName("TC-SEC-13: Admin quản lý user")
    class AdminUserManagementTests {

        @Test
        @DisplayName("TC-SEC-13: Admin không thể sửa tài khoản Admin")
        void updateUser_adminAccount_throwsException() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            // When / Then
            assertThatThrownBy(() -> adminUserService.updateUser(1L, "New Name", "0909", "Address", null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "FORBIDDEN");
        }

        @Test
        @DisplayName("TC-SEC-13: Admin không thể ẩn/hiện tài khoản Admin")
        void toggleVisibility_adminAccount_throwsException() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            // When / Then
            assertThatThrownBy(() -> adminUserService.toggleUserVisibility(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "FORBIDDEN");
        }

        @Test
        @DisplayName("TC-SEC-17: Tạo Staff với role ADMIN trong body - vẫn tạo STAFF")
        void createStaff_roleInBodyIgnored_createsStaff() {
            // Given
            CreateStaffRequest request = new CreateStaffRequest("New Staff", "newstaff@example.com", "password123", "0909", "Address");
            when(userRepository.existsByEmail("newstaff@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(4L);
                return u;
            });

            // When
            UserResponse response = adminUserService.createStaff(request);

            // Then
            assertThat(response.role()).isEqualTo("STAFF");
            assertThat(response.email()).isEqualTo("newstaff@example.com");
        }

        @Test
        @DisplayName("TC-SEC-17: Tạo Staff email trùng - ném BusinessException EMAIL_EXISTS")
        void createStaff_emailExists_throwsException() {
            // Given
            CreateStaffRequest request = new CreateStaffRequest("New Staff", "staff@example.com", "password123", null, null);
            when(userRepository.existsByEmail("staff@example.com")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> adminUserService.createStaff(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "EMAIL_EXISTS");
        }

        @Test
        @DisplayName("Sửa user thường thành công")
        void updateUser_normalUser_success() {
            // Given
            when(userRepository.findById(2L)).thenReturn(Optional.of(staffUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            UserResponse response = adminUserService.updateUser(2L, "Updated Staff", "0912345678", "New Address", null);

            // Then
            assertThat(response.fullName()).isEqualTo("Updated Staff");
            assertThat(response.phone()).isEqualTo("0912345678");
            assertThat(response.address()).isEqualTo("New Address");
            verify(userRepository).save(staffUser);
        }

        @Test
        @DisplayName("Ẩn/hiện user thường thành công")
        void toggleVisibility_normalUser_success() {
            // Given
            when(userRepository.findById(2L)).thenReturn(Optional.of(staffUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            adminUserService.toggleUserVisibility(2L);

            // Then
            assertThat(staffUser.isEnabled()).isFalse();
            verify(userRepository).save(staffUser);
        }
    }

    @Nested
    @DisplayName("Danh sách user")
    class UserListTests {

        @Test
        @DisplayName("Lấy danh sách user có phân trang")
        void getUsers_paged() {
            // Given
            Page<User> page = new PageImpl<>(List.of(adminUser, staffUser, customerUser));
            when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            Page<UserResponse> result = adminUserService.getUsers(null, null, Pageable.unpaged());

            // Then
            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("Lọc user theo role")
        void getUsers_filterByRole() {
            // Given
            Page<User> page = new PageImpl<>(List.of(staffUser));
            when(userRepository.findByRole(eq(Role.STAFF), any(Pageable.class))).thenReturn(page);

            // When
            Page<UserResponse> result = adminUserService.getUsers(null, Role.STAFF, Pageable.unpaged());

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).role()).isEqualTo("STAFF");
        }
    }
}