package vn.edu.iuh.keymech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.iuh.keymech.dto.CheckoutRequest;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.model.Order;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.PaymentMethod;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.OrderRepository;
import vn.edu.iuh.keymech.repository.OrderStockRepository;
import vn.edu.iuh.keymech.repository.ProductRepository;
import vn.edu.iuh.keymech.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock UserRepository users;
    @Mock ProductRepository products;
    @Mock OrderStockRepository stock;
    @Mock OrderRepository orders;
    @InjectMocks OrderService service;

    private final CheckoutRequest request = new CheckoutRequest(
        "Nguyễn An", "0901234567", "12 Nguyễn Trãi", PaymentMethod.COD);

    @Test
    @DisplayName("TC-PAY-01: giỏ rỗng trả 400 và không tạo đơn")
    void placeOrder_emptyCart_returnsBadRequest() {
        BusinessException error = assertThrows(BusinessException.class,
            () -> service.placeOrder(7L, request, Map.of()));
        assertEquals("EMPTY_CART", error.getCode());
        assertEquals(400, error.getStatus().value());
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("TC-PAY-02: giá đơn lấy từ CSDL và trừ tồn có điều kiện")
    void placeOrder_validCart_usesDatabasePriceAndConditionalUpdate() {
        givenCustomer();
        Product product = product(3L, "Keychron K2", 2_200_000);
        when(products.findById(3L)).thenReturn(Optional.of(product));
        when(stock.decreaseStock(3L, 2)).thenReturn(1);
        when(orders.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        OrderService.PlacedOrder placed = service.placeOrder(7L, request, Map.of(3L, 2));

        assertEquals(new BigDecimal("4400000"), placed.response().totalAmount());
        assertEquals(99L, placed.response().id());
        assertEquals(1, placed.items().size());
        verify(stock).decreaseStock(3L, 2);
        verify(orders).save(any(Order.class));
    }

    @Test
    @DisplayName("TC-PAY-03: thiếu tồn trả 409 và không lưu đơn")
    void placeOrder_outOfStock_doesNotSaveOrder() {
        givenCustomer();
        when(products.findById(3L)).thenReturn(Optional.of(product(3L, "Keychron K2", 2_200_000)));
        when(stock.decreaseStock(3L, 2)).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
            () -> service.placeOrder(7L, request, Map.of(3L, 2)));

        assertEquals("OUT_OF_STOCK", error.getCode());
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("TC-PAY-04: số lượng không hợp lệ không trừ tồn")
    void placeOrder_nonPositiveQuantity_doesNotTouchStock() {
        givenCustomer();
        BusinessException error = assertThrows(BusinessException.class,
            () -> service.placeOrder(7L, request, Map.of(3L, 0)));
        assertEquals("OUT_OF_STOCK", error.getCode());
        verify(stock, never()).decreaseStock(any(), anyInt());
    }

    private void givenCustomer() {
        User user = new User();
        user.setId(7L);
        user.setEmail("an@example.com");
        user.setRole(Role.CUSTOMER);
        when(users.findById(7L)).thenReturn(Optional.of(user));
    }

    private Product product(Long id, String name, long price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(BigDecimal.valueOf(price));
        return product;
    }
}
