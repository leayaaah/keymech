package vn.edu.iuh.keymech.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.dto.product.ProductsDetailResponse;
import vn.edu.iuh.keymech.dto.product.ProductsFilterRequest;
import vn.edu.iuh.keymech.dto.product.ProductsResponse;
import vn.edu.iuh.keymech.service.ProductService;

/**
 * G-01: Danh sách sản phẩm (lọc, phân trang).
 * G-02: Chi tiết sản phẩm.
 * Public — không cần đăng nhập.
 */
@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products
     * Query params: categoryId, brand, layout, minPrice, maxPrice, keyword, sort, page, size
     * Trang rỗng trả về content rỗng (không ném lỗi).
     */
    @GetMapping("/api/products")
    public ResponseEntity<PageResponse<ProductsResponse>> list(@ModelAttribute ProductsFilterRequest f) {
        return ResponseEntity.ok(PageResponse.of(productService.list(f)));
    }

    /**
     * GET /api/products/{id}
     * Sản phẩm đã ẩn hoặc không tồn tại -> 404.
     */
    @GetMapping("/api/products/{id}")
    public ResponseEntity<ProductsDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.detail(id));
    }
}