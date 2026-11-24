package vn.edu.iuh.keymech.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.CategoryRepository;
import vn.edu.iuh.keymech.repository.ProductRepository;
import vn.edu.iuh.keymech.repository.UserRepository;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.service.OrderEmailService;
import vn.edu.iuh.keymech.util.SessionKeys;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class CheckoutPurchaseIntegrationTests {

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @MockitoBean OrderEmailService email;

    private Long customerId;
    private Long categoryId;
    private Long productId;

    @Test
    void rejectedPurchaseWriteDoesNotUndoCheckout(CapturedOutput output) throws Exception {
        String suffix = UUID.randomUUID().toString();
        User customer = new User();
        customer.setFullName("Checkout Test");
        customer.setEmail("purchase-" + suffix + "@example.test");
        customer.setPassword("test-only");
        customer.setRole(Role.CUSTOMER);
        customerId = users.save(customer).getId();

        Category category = new Category();
        category.setName("Checkout Test");
        category.setSlug("purchase-" + suffix);
        categoryId = categories.save(category).getId();

        Product product = new Product();
        product.setCategory(category);
        product.setName("Checkout Test");
        product.setPrice(BigDecimal.valueOf(250_000));
        product.setStockQuantity(5);
        productId = products.save(product).getId();

        var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        var principal = user(new AppUserDetails(customer));
        var session = new MockHttpSession();
        mvc.perform(post("/api/cart/items").with(principal).session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId": %d, "quantity": 2}
                    """.formatted(productId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].quantity").value(2));
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertActivityCount("ADD_TO_CART", 1));

        // MariaDB rejects the next tracking insert because visitor_id allows at most 36 characters.
        session.setAttribute(SessionKeys.VISITOR_ID, "x".repeat(37));
        String response = mvc.perform(post("/api/orders/checkout").with(principal).session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"receiverName":"Checkout Test","receiverPhone":"0900000000",
                     "shippingAddress":"Test Address","paymentMethod":"COD"}
                    """))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.totalAmount").value(500_000))
            .andReturn().getResponse().getContentAsString();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(output.getAll())
            .contains("Data too long for column 'visitor_id'", "Could not persist user activity"));
        assertActivityCount("PURCHASE", 0);

        Number orderId = JsonPath.read(response, "$.id");
        String orderCode = JsonPath.read(response, "$.orderCode");
        assertThat(jdbc.queryForObject("SELECT order_code FROM orders WHERE id=? AND user_id=?",
            String.class, orderId.longValue(), customerId)).isEqualTo(orderCode);
        assertThat(jdbc.queryForObject("SELECT quantity FROM order_items WHERE order_id=? AND product_id=?",
            Integer.class, orderId.longValue(), productId)).isEqualTo(2);
        assertThat(products.findById(productId).orElseThrow().getStockQuantity()).isEqualTo(3);
        assertThat(session.getAttribute(SessionKeys.CART)).isNull();
        mvc.perform(get("/api/cart").with(principal).session(session))
            .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
        verify(email).sendConfirmation(argThat(placed -> placed.response().id().equals(orderId.longValue())));
    }

    private void assertActivityCount(String event, int count) {
        assertThat(jdbc.queryForObject("""
            SELECT COUNT(*) FROM user_activity WHERE user_id=? AND product_id=? AND event_type=?
            """, Integer.class, customerId, productId, event)).isEqualTo(count);
    }

    @AfterEach
    void cleanUpCommittedTestFixtures() {
        jdbc.update("DELETE FROM user_activity WHERE user_id=?", customerId);
        jdbc.update("DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE user_id=?)", customerId);
        jdbc.update("DELETE FROM orders WHERE user_id=?", customerId);
        jdbc.update("DELETE FROM products WHERE id=?", productId);
        jdbc.update("DELETE FROM categories WHERE id=?", categoryId);
        jdbc.update("DELETE FROM users WHERE id=?", customerId);
    }
}
