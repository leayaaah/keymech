package vn.edu.iuh.keymech.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.function.Function;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;
import vn.edu.iuh.keymech.dto.cart.AddToCartRequest;
import vn.edu.iuh.keymech.dto.cart.CartResponse;
import vn.edu.iuh.keymech.dto.cart.UpdateCartItemRequest;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.CartService;
import vn.edu.iuh.keymech.tracking.ActivityTracker;
import vn.edu.iuh.keymech.util.SessionKeys;

/**
 * G-03: Giỏ hàng — chỉ Guest và Customer (Staff/Admin bị 403).
 * Dữ liệu lưu trong Session, không ghi CSDL.
 */
@RestController
public class CartController {

    private final CartService cartService;
    private final ActivityTracker activityTracker;

    public CartController(CartService cartService, ActivityTracker activityTracker) {
        this.cartService = cartService;
        this.activityTracker = activityTracker;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserDetails ud) {
            return ud.getId();
        }
        return null;
    }

    /**
     * GET /api/cart — xem giỏ, giá lấy lại từ CSDL.
     */
    @GetMapping("/api/cart")
    public CartResponse getCart(HttpSession session) {
        return withCartSession(session, cartService::getCart);
    }

    /**
     * POST /api/cart/items — thêm sản phẩm (cộng dồn).
     * Sản phẩm đã ẩn → 404; vượt tồn kho → 409 OUT_OF_STOCK.
     */
    @PostMapping("/api/cart/items")
    public CartResponse addToCart(@Valid @RequestBody AddToCartRequest req, HttpSession session) {
        CartResponse result = withCartSession(session, cartSession -> cartService.addToCart(req, cartSession));
        // Ghi tracking ADD_TO_CART
        activityTracker.record(EventType.ADD_TO_CART, (String) session.getAttribute(SessionKeys.VISITOR_ID), currentUserId(),
                req.productId(), null, null);
        return result;
    }

    /**
     * PUT /api/cart/items/{productId} — sửa số lượng.
     */
    @PutMapping("/api/cart/items/{productId}")
    public CartResponse updateItem(@PathVariable Long productId,
                                   @Valid @RequestBody UpdateCartItemRequest req, HttpSession session) {
        return withCartSession(session, cartSession -> cartService.updateItem(productId, req.quantity(), cartSession));
    }

    /**
     * DELETE /api/cart/items/{productId} — xóa một mặt hàng.
     */
    @DeleteMapping("/api/cart/items/{productId}")
    public CartResponse removeItem(@PathVariable Long productId, HttpSession session) {
        return withCartSession(session, cartSession -> cartService.removeItem(productId, cartSession));
    }

    /**
     * DELETE /api/cart — làm rỗng giỏ.
     */
    @DeleteMapping("/api/cart")
    public CartResponse clearCart(HttpSession session) {
        return withCartSession(session, cartService::clearCart);
    }

    private CartResponse withCartSession(HttpSession session, Function<HttpSession, CartResponse> operation) {
        // Dùng cùng khóa với checkout để cập nhật giỏ không xen vào lúc tạo đơn.
        synchronized (WebUtils.getSessionMutex(session)) {
            return operation.apply(session);
        }
    }
}
