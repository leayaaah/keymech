package vn.edu.iuh.keymech.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.RequestContextHolder;

import vn.edu.iuh.keymech.dto.cart.AddToCartRequest;
import vn.edu.iuh.keymech.dto.cart.CartResponse;
import vn.edu.iuh.keymech.dto.cart.UpdateCartItemRequest;
import vn.edu.iuh.keymech.service.CartService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * G-03: Giỏ hàng — chỉ Guest và Customer (Staff/Admin bị 403).
 * Dữ liệu lưu trong Session, không ghi CSDL.
 */
@RestController
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * GET /api/cart — xem giỏ, giá lấy lại từ CSDL.
     */
    @GetMapping("/api/cart")
    public CartResponse getCart() {
        return cartService.getCart(currentSession());
    }

    /**
     * POST /api/cart/items — thêm sản phẩm (cộng dồn).
     * Sản phẩm đã ẩn → 404; vượt tồn kho → 409 OUT_OF_STOCK.
     */
    @PostMapping("/api/cart/items")
    public CartResponse addToCart(@RequestBody AddToCartRequest req) {
        return cartService.addToCart(req, currentSession());
    }

    /**
     * PUT /api/cart/items/{productId} — sửa số lượng.
     */
    @PutMapping("/api/cart/items/{productId}")
    public CartResponse updateItem(@PathVariable Long productId,
                                   @RequestBody UpdateCartItemRequest req) {
        return cartService.updateItem(productId, req.quantity(), currentSession());
    }

    /**
     * DELETE /api/cart/items/{productId} — xóa một mặt hàng.
     */
    @DeleteMapping("/api/cart/items/{productId}")
    public CartResponse removeItem(@PathVariable Long productId) {
        return cartService.removeItem(productId, currentSession());
    }

    /**
     * DELETE /api/cart — làm rỗng giỏ.
     */
    @DeleteMapping("/api/cart")
    public CartResponse clearCart() {
        return cartService.clearCart(currentSession());
    }

    private HttpSession currentSession() {
        HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
        return request != null ? request.getSession() : null;
    }
}