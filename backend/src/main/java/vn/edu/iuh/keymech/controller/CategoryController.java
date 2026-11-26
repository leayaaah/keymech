package vn.edu.iuh.keymech.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.keymech.dto.CategoryResponse;
import vn.edu.iuh.keymech.service.CatalogManagementService;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CatalogManagementService catalog;

    @GetMapping("/api/categories")
    public List<CategoryResponse> publicList() {
        return catalog.listCategories(true);
    }

    @GetMapping("/api/admin/categories")
    public List<CategoryResponse> adminList() {
        return catalog.listCategories(false);
    }

    @PostMapping("/api/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryResponse.Create input) {
        return catalog.createCategory(input);
    }

    @PutMapping("/api/admin/categories/{id}/visibility")
    public CategoryResponse visibility(@PathVariable long id, @Valid @RequestBody CategoryResponse.Visibility input) {
        return catalog.changeCategoryVisibility(id, input.status());
    }
}
