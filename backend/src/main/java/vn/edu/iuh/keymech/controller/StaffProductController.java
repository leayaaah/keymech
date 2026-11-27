package vn.edu.iuh.keymech.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.keymech.dto.CategoryResponse;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.dto.product.StaffProductInput;
import vn.edu.iuh.keymech.dto.product.StaffProductResponse;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;
import vn.edu.iuh.keymech.service.CatalogManagementService;

@RestController
@RequiredArgsConstructor
public class StaffProductController {
    private final CatalogManagementService catalog;

    @GetMapping("/api/staff/products")
    public PageResponse<StaffProductResponse> list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) VisibilityStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return catalog.listProducts(keyword, status, page, size);
    }

    @GetMapping("/api/staff/products/{id}")
    public StaffProductResponse detail(@PathVariable long id) {
        return catalog.getProduct(id);
    }

    @PostMapping(path = "/api/staff/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public StaffProductResponse create(@Valid @ModelAttribute StaffProductInput input) {
        return catalog.createProduct(input);
    }

    @PutMapping(path = "/api/staff/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StaffProductResponse update(@PathVariable long id, @Valid @ModelAttribute StaffProductInput input) {
        return catalog.updateProduct(id, input);
    }

    @PutMapping("/api/admin/products/{id}/visibility")
    public StaffProductResponse visibility(@PathVariable long id, @Valid @RequestBody CategoryResponse.Visibility input) {
        return catalog.changeProductVisibility(id, input.status());
    }
}
