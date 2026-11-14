package vn.edu.iuh.keymech.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.keymech.dto.ChangePasswordRequest;
import vn.edu.iuh.keymech.dto.MessageResponse;
import vn.edu.iuh.keymech.dto.UpdateProfileRequest;
import vn.edu.iuh.keymech.dto.UserResponse;
import vn.edu.iuh.keymech.service.AuthService;

@RestController
@RequestMapping("/api/users/me")
public class UserMeController {

    private final AuthService authService;

    public UserMeController(AuthService authService) {
        this.authService = authService;
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                           Authentication authentication) {
        authService.changePassword(request, authentication);
        return ResponseEntity.ok(new MessageResponse("Đổi mật khẩu thành công"));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                       Authentication authentication) {
        UserResponse response = authService.updateProfile(request, authentication);
        return ResponseEntity.ok(response);
    }
}
