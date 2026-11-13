package vn.edu.iuh.keymech.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;
import vn.edu.iuh.keymech.dto.CheckoutRequest;
import vn.edu.iuh.keymech.dto.CheckoutResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.OrderEmailService;
import vn.edu.iuh.keymech.service.OrderService;
import vn.edu.iuh.keymech.util.SessionKeys;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderEmailService orderEmailService;

    public OrderController(OrderService orderService, OrderEmailService orderEmailService) {
        this.orderService = orderService;
        this.orderEmailService = orderEmailService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest body,
                                                      @AuthenticationPrincipal AppUserDetails principal,
                                                      HttpServletRequest request) {
        if (principal == null) {
            throw new AccessDeniedException("Vui lòng đăng nhập");
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw emptyCart();
        }
        synchronized (WebUtils.getSessionMutex(session)) {
            @SuppressWarnings("unchecked")
            Map<Long, Integer> storedCart = (Map<Long, Integer>) session.getAttribute(SessionKeys.CART);
            if (storedCart == null || storedCart.isEmpty()) {
                throw emptyCart();
            }
            OrderService.PlacedOrder placed = orderService.placeOrder(
                principal.getId(), body, new HashMap<>(storedCart));
            // Lời gọi service đã commit. Lỗi email không làm đổi kết quả đặt hàng.
            session.removeAttribute(SessionKeys.CART);
            try {
                orderEmailService.sendConfirmation(placed);
            } catch (RuntimeException e) {
                log.warn("Không thể lên lịch email xác nhận đơn {}", placed.response().orderCode(), e);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(placed.response());
        }
    }

    private BusinessException emptyCart() {
        return new BusinessException(HttpStatus.BAD_REQUEST, "EMPTY_CART", "Giỏ hàng đang trống");
    }
}
