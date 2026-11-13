package vn.edu.iuh.keymech.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;

import vn.edu.iuh.keymech.dto.cart.AddToCartRequest;
import vn.edu.iuh.keymech.dto.cart.CartItemResponse;
import vn.edu.iuh.keymech.dto.cart.CartResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;
import vn.edu.iuh.keymech.util.SessionKeys;

/**
 * G-03: Giỏ hàng — chỉ lưu id và số lượng trong Session.
 * Giá luôn lấy lại từ CSDL; sản phẩm đã ẩn được đánh dấu unavailable.
 * Chỉ dành cho Guest và Customer (Staff/Admin bị 403 ở CartAccessManager).
 */
@Service
public class CartService {

    private final ProductService productService;

    public CartService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lấy giỏ hàng hiện tại từ Session, giá lấy lại từ CSDL.
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SessionKeys.CART);
        if (cart == null || cart.isEmpty()) {
            return new CartResponse(new ArrayList<>(), BigDecimal.ZERO);
        }

        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> e : cart.entrySet()) {
            Product p = productService.findById(e.getKey());
            int qty = e.getValue();
            boolean available = p.getStatus() == VisibilityStatus.ACTIVE
                    && p.getStockQuantity() >= qty;
            BigDecimal subtotal = available
                    ? p.getPrice().multiply(BigDecimal.valueOf(qty))
                    : BigDecimal.ZERO;
            if (available) {
                total = total.add(subtotal);
            }

            items.add(new CartItemResponse(
                    p.getId(),
                    p.getName(),
                    p.getImageUrl(),
                    p.getPrice(),
                    qty,
                    subtotal,
                    p.getStockQuantity(),
                    available));
        }

        return new CartResponse(items, total.setScale(0, RoundingMode.DOWN));
    }

    /**
     * Thêm sản phẩm vào giỏ (cộng dồn số lượng).
     * Sản phẩm đã ẩn → 404; vượt tồn kho → 409 OUT_OF_STOCK.
     */
    @Transactional
    public CartResponse addToCart(AddToCartRequest req, HttpSession session) {
        Product p = productService.findById(req.productId());
        if (p.getStatus() != VisibilityStatus.ACTIVE) {
            throw new ResourceNotFoundException("Sản phẩm không còn hiển thị");
        }
        if (p.getStockQuantity() < req.quantity()) {
            throw new BusinessException("OUT_OF_STOCK", "Sản phẩm không đủ hàng");
        }

        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = getOrCreateCart(session);
        int current = cart.getOrDefault(p.getId(), 0);
        int newQty = current + req.quantity();
        if (newQty > p.getStockQuantity()) {
            throw new BusinessException("OUT_OF_STOCK", "Số lượng vượt quá tồn kho");
        }
        cart.put(p.getId(), newQty);
        session.setAttribute(SessionKeys.CART, cart);

        return getCart(session);
    }

    /**
     * Cập nhật số lượng một mặt hàng.
     */
    @Transactional
    public CartResponse updateItem(Long productId, int quantity, HttpSession session) {
        if (quantity < 1) {
            throw new BusinessException("INVALID_QUANTITY", "Số lượng phải từ 1");
        }

        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SessionKeys.CART);
        if (cart == null || !cart.containsKey(productId)) {
            throw new ResourceNotFoundException("Sản phẩm không có trong giỏ");
        }

        Product p = productService.findById(productId);
        if (p.getStatus() != VisibilityStatus.ACTIVE) {
            throw new ResourceNotFoundException("Sản phẩm không còn hiển thị");
        }
        if (quantity > p.getStockQuantity()) {
            throw new BusinessException("OUT_OF_STOCK", "Sản phẩm không đủ hàng");
        }

        cart.put(productId, quantity);
        session.setAttribute(SessionKeys.CART, cart);

        return getCart(session);
    }

    /**
     * Xóa một mặt hàng khỏi giỏ.
     */
    @Transactional
    public CartResponse removeItem(Long productId, HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SessionKeys.CART);
        if (cart == null || cart.isEmpty()) {
            return new CartResponse(new ArrayList<>(), BigDecimal.ZERO);
        }
        cart.remove(productId);
        session.setAttribute(SessionKeys.CART, cart);
        return getCart(session);
    }

    /**
     * Làm rỗng giỏ.
     */
    @Transactional
    public CartResponse clearCart(HttpSession session) {
        session.removeAttribute(SessionKeys.CART);
        return new CartResponse(new ArrayList<>(), BigDecimal.ZERO);
    }

    /**
     * Lấy snapshot giỏ (dùng cho checkout).
     */
    @SuppressWarnings("unchecked")
    public Map<Long, Integer> getCartSnapshot(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SessionKeys.CART);
        return cart == null ? new HashMap<>() : new HashMap<>(cart);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getOrCreateCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SessionKeys.CART);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(SessionKeys.CART, cart);
        }
        return cart;
    }
}