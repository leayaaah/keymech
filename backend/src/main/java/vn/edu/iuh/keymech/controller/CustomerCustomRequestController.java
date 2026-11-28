package vn.edu.iuh.keymech.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.keymech.dto.CustomRequestInput;
import vn.edu.iuh.keymech.dto.CustomKitOption;
import vn.edu.iuh.keymech.dto.CustomRequestResponse;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.CustomCatalogService;
import vn.edu.iuh.keymech.service.CustomRequestService;

@RestController
@RequestMapping("/api/custom-requests")
@RequiredArgsConstructor
public class CustomerCustomRequestController {
    private final CustomRequestService requests;
    private final CustomCatalogService catalog;

    @GetMapping("/components")
    public List<CustomKitOption> components() {
        return catalog.options();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomRequestResponse create(@AuthenticationPrincipal AppUserDetails customer,
                                        @Valid @RequestBody CustomRequestInput input) {
        return requests.create(customer.getId(), input);
    }

    @GetMapping("/my")
    public PageResponse<CustomRequestResponse> list(@AuthenticationPrincipal AppUserDetails customer,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return requests.listMine(customer.getId(), page, size);
    }

    @GetMapping("/my/{id}")
    public CustomRequestResponse detail(@AuthenticationPrincipal AppUserDetails customer, @PathVariable long id) {
        return requests.getMine(customer.getId(), id);
    }

    @PutMapping("/my/{id}/accept")
    public CustomRequestResponse accept(@AuthenticationPrincipal AppUserDetails customer, @PathVariable long id,
                                        @Valid @RequestBody CustomRequestInput.Accept input) {
        return requests.accept(customer.getId(), id, input.quotedPrice());
    }

    @PutMapping("/my/{id}/decline")
    public CustomRequestResponse decline(@AuthenticationPrincipal AppUserDetails customer, @PathVariable long id,
                                         @Valid @RequestBody CustomRequestInput.Decline input) {
        return requests.decline(customer.getId(), id, input.note());
    }
}
