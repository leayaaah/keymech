package vn.edu.iuh.keymech.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.keymech.dto.*;
import vn.edu.iuh.keymech.model.enums.CustomStatus;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.CustomRequestService;

/**
 * Quản lý yêu cầu Custom phía Staff và Admin (A-07).
 * SecurityConfig: /api/staff/** yêu cầu ROLE_STAFF hoặc ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/staff/custom-requests")
public class StaffCustomRequestController {

    private final CustomRequestService service;

    public StaffCustomRequestController(CustomRequestService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CustomRequestResponse> list(@RequestParam(required = false) CustomStatus status,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return service.listForStaff(status, page, size);
    }

    @GetMapping("/{id}")
    public CustomRequestDetailResponse detail(@PathVariable Long id) {
        return service.getForStaff(id);
    }

    @PutMapping("/{id}/quote")
    public CustomRequestResponse quote(@PathVariable Long id, @Valid @RequestBody QuoteRequest body) {
        return service.quote(id, body.quotedPrice());
    }

    @PutMapping("/{id}/status")
    public CustomRequestResponse changeStatus(@AuthenticationPrincipal AppUserDetails me,
                                              @PathVariable Long id,
                                              @Valid @RequestBody ChangeCustomStatusRequest body) {
        return service.changeStatus(id, body.status(), body.note(), me.getId());
    }
}
