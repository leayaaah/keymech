package vn.edu.iuh.keymech.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.keymech.dto.*;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.CustomRequestService;

/**
 * Yêu cầu Custom phía Customer (C-03, C-04, C-06). Chỉ Customer vào được
 * (SecurityConfig: /api/custom-requests/** yêu cầu ROLE_CUSTOMER).
 * Controller chỉ nhận request, validate và gọi Service; kiểm quyền sở hữu nằm ở Service.
 */
@RestController
@RequestMapping("/api/custom-requests")
public class CustomRequestController {

    private final CustomRequestService service;

    public CustomRequestController(CustomRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomRequestResponse> create(@AuthenticationPrincipal AppUserDetails me,
                                                        @Valid @RequestBody CreateCustomRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(me.getId(), body));
    }

    @GetMapping("/my")
    public PageResponse<CustomRequestResponse> listMy(@AuthenticationPrincipal AppUserDetails me,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return service.listMy(me.getId(), page, size);
    }

    @GetMapping("/my/{id}")
    public CustomRequestDetailResponse getMy(@AuthenticationPrincipal AppUserDetails me,
                                             @PathVariable Long id) {
        return service.getMy(id, me.getId());
    }

    @PutMapping("/my/{id}/accept")
    public CustomRequestResponse accept(@AuthenticationPrincipal AppUserDetails me,
                                        @PathVariable Long id,
                                        @Valid @RequestBody AcceptQuoteRequest body) {
        return service.accept(id, me.getId(), body.quotedPrice());
    }

    @PutMapping("/my/{id}/decline")
    public CustomRequestResponse decline(@AuthenticationPrincipal AppUserDetails me,
                                         @PathVariable Long id,
                                         @Valid @RequestBody(required = false) DeclineRequest body) {
        return service.decline(id, me.getId(), body == null ? null : body.note());
    }
}
