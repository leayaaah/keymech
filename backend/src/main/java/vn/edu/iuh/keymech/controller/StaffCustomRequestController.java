package vn.edu.iuh.keymech.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.keymech.dto.CustomRequestInput;
import vn.edu.iuh.keymech.dto.CustomRequestResponse;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.model.enums.CustomStatus;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.CustomRequestService;

@RestController
@RequestMapping("/api/staff/custom-requests")
@RequiredArgsConstructor
public class StaffCustomRequestController {
    private final CustomRequestService requests;

    @GetMapping
    public PageResponse<CustomRequestResponse> list(@RequestParam(required = false) CustomStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return requests.listForStaff(status, page, size);
    }

    @GetMapping("/{id}")
    public CustomRequestResponse detail(@PathVariable long id) {
        return requests.getForStaff(id);
    }

    @PutMapping("/{id}/quote")
    public CustomRequestResponse quote(@PathVariable long id, @AuthenticationPrincipal AppUserDetails staff,
                                       @Valid @RequestBody CustomRequestInput.Quote input) {
        return requests.quote(id, staff.getId(), input);
    }

    @PutMapping("/{id}/status")
    public CustomRequestResponse status(@PathVariable long id, @AuthenticationPrincipal AppUserDetails staff,
                                        @Valid @RequestBody CustomRequestInput.Transition input) {
        return requests.changeStatus(id, staff.getId(), input);
    }
}
