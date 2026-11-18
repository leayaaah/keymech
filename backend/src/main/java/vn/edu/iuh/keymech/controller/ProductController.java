package vn.edu.iuh.keymech.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.dto.product.ProductsDetailResponse;
import vn.edu.iuh.keymech.dto.product.ProductsFilterRequest;
import vn.edu.iuh.keymech.dto.product.ProductsResponse;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.ProductService;
import vn.edu.iuh.keymech.tracking.ActivityTracker;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.util.SessionKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * G-01: Danh sách sản phẩm (lọc, phân trang).
 * G-02: Chi tiết sản phẩm.
 * Public — không cần đăng nhập.
 */
@RestController
public class ProductController {

    private final ProductService productService;
    private final ActivityTracker activityTracker;

    public ProductController(ProductService productService, ActivityTracker activityTracker) {
        this.productService = productService;
        this.activityTracker = activityTracker;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserDetails ud) {
            return ud.getId();
        }
        return null;
    }

    private String currentVisitorId() {
        HttpSession session = currentSession();
        if (session != null) {
            return (String) session.getAttribute(SessionKeys.VISITOR_ID);
        }
        return null;
    }

    private HttpSession currentSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        return request != null ? request.getSession(false) : null;
    }

    /**
     * GET /api/products
     * Query params: categoryId, brand, layout, minPrice, maxPrice, keyword, sort, page, size
     * Trang rỗng trả về content rỗng (không ném lỗi).
     */
    @GetMapping("/api/products")
    public ResponseEntity<PageResponse<ProductsResponse>> list(@ModelAttribute ProductsFilterRequest f) {
        PageResponse<ProductsResponse> result = PageResponse.of(productService.list(f));
        // Ghi tracking SEARCH (nếu có keyword) hoặc VIEW_PRODUCT (danh sách)
        if (f.keyword() != null && !f.keyword().isBlank()) {
            activityTracker.record(EventType.SEARCH, currentVisitorId(), currentUserId(),
                    null, f.categoryId(), f.keyword());
        } else {
            activityTracker.record(EventType.VIEW_PRODUCT, currentVisitorId(), currentUserId(),
                    null, f.categoryId(), null);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/products/{id}
     * Sản phẩm đã ẩn hoặc không tồn tại -> 404.
     */
    @GetMapping("/api/products/{id}")
    public ResponseEntity<ProductsDetailResponse> detail(@PathVariable Long id) {
        ProductsDetailResponse result = productService.detail(id);
        // Ghi tracking VIEW_PRODUCT chi tiết
        activityTracker.record(EventType.VIEW_PRODUCT, currentVisitorId(), currentUserId(),
                id, null, null);
        return ResponseEntity.ok(result);
    }
}