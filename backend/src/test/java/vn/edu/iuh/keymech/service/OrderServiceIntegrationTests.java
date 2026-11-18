package vn.edu.iuh.keymech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.CheckoutRequest;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.PaymentMethod;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.CategoryRepository;
import vn.edu.iuh.keymech.repository.OrderRepository;
import vn.edu.iuh.keymech.repository.ProductRepository;
import vn.edu.iuh.keymech.repository.UserRepository;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTests {

    @Autowired OrderService service;
    @Autowired UserRepository users;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired EntityManager entityManager;

    private final CheckoutRequest request = new CheckoutRequest(
        "Nguyễn An", "0901234567", "12 Nguyễn Trãi", PaymentMethod.COD);

    @Test
    void persistsOrderItemsAndDecreasesStock() {
        User user = customer();
        Product product = product(3, 1_500_000);
        entityManager.flush();
        entityManager.clear();

        var placed = service.placeOrder(user.getId(), request, Map.of(product.getId(), 2));
        entityManager.flush();
        entityManager.clear();

        var saved = orders.findById(placed.response().id()).orElseThrow();
        assertEquals(BigDecimal.valueOf(3_000_000), saved.getTotalAmount());
        assertEquals(1, saved.getItems().size());
        assertEquals(2, saved.getItems().getFirst().getQuantity());
        assertEquals(1, products.findById(product.getId()).orElseThrow().getStockQuantity());
    }

    private User customer() {
        String suffix = UUID.randomUUID().toString();
        User user = new User();
        user.setFullName("Checkout test " + suffix);
        user.setEmail("checkout-" + suffix + "@example.test");
        user.setPassword("test-only");
        user.setRole(Role.CUSTOMER);
        return users.save(user);
    }

    private Product product(int stock, long price) {
        String suffix = UUID.randomUUID().toString();
        Category category = new Category();
        category.setName("Checkout test " + suffix);
        category.setSlug("checkout-test-" + suffix);
        categories.save(category);
        Product product = new Product();
        product.setCategory(category);
        product.setName("Checkout product " + suffix);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStockQuantity(stock);
        return products.save(product);
    }
}
