package vn.edu.iuh.keymech.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import vn.edu.iuh.keymech.dto.CheckoutRequest;
import vn.edu.iuh.keymech.dto.CheckoutResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.OrderStatus;
import vn.edu.iuh.keymech.model.enums.PaymentMethod;
import vn.edu.iuh.keymech.model.enums.PaymentStatus;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.OrderEmailService;
import vn.edu.iuh.keymech.service.OrderService;
import vn.edu.iuh.keymech.util.SessionKeys;

class OrderControllerTest {

    private final OrderService orders = org.mockito.Mockito.mock(OrderService.class);
    private final OrderEmailService email = org.mockito.Mockito.mock(OrderEmailService.class);
    private final OrderController controller = new OrderController(orders, email);
    private final CheckoutRequest body = new CheckoutRequest("Nguyễn An", "0901234567", "Hà Nội", PaymentMethod.COD);

    @Test
    @DisplayName("TC-PAY-05: chỉ xóa giỏ và gửi email sau khi service trả thành công")
    void checkout_success_clearsCartAndSchedulesEmail() {
        MockHttpServletRequest request = requestWithCart();
        OrderService.PlacedOrder result = placedOrder();
        when(orders.placeOrder(eq(7L), eq(body), any())).thenReturn(result);

        var response = controller.checkout(body, customer(), request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(null, request.getSession().getAttribute(SessionKeys.CART));
        verify(email).sendConfirmation(result);
    }

    @Test
    @DisplayName("TC-PAY-06: giao dịch lỗi thì giữ giỏ và không gửi email")
    void checkout_failure_keepsCart() {
        MockHttpServletRequest request = requestWithCart();
        when(orders.placeOrder(eq(7L), eq(body), any()))
            .thenThrow(new BusinessException("OUT_OF_STOCK", "Sản phẩm không đủ hàng"));

        assertThrows(BusinessException.class, () -> controller.checkout(body, customer(), request));

        assertEquals(1, ((LinkedHashMap<?, ?>) request.getSession().getAttribute(SessionKeys.CART)).size());
        verify(email, never()).sendConfirmation(any());
    }

    private MockHttpServletRequest requestWithCart() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.CART, new LinkedHashMap<>(java.util.Map.of(3L, 1)));
        request.setSession(session);
        return request;
    }

    private AppUserDetails customer() {
        User user = new User();
        user.setId(7L);
        user.setEmail("an@example.com");
        user.setPassword("hashed");
        user.setRole(Role.CUSTOMER);
        return new AppUserDetails(user);
    }

    private OrderService.PlacedOrder placedOrder() {
        CheckoutResponse response = new CheckoutResponse(99L, "ORD-TEST00001", OrderStatus.NEW,
            PaymentStatus.UNPAID, BigDecimal.valueOf(2_200_000));
        return new OrderService.PlacedOrder(response, "an@example.com", LocalDateTime.now(),
            body.receiverName(), body.receiverPhone(), body.shippingAddress(), "COD", List.of());
    }
}
