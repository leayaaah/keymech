package vn.edu.iuh.keymech.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.Order;
import vn.edu.iuh.keymech.model.OrderItem;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.OrderStatus;
import vn.edu.iuh.keymech.model.enums.PaymentMethod;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;

@SpringBootTest
@Transactional
class StaffOrderIntegrationTests {

    @Autowired WebApplicationContext context;
    @Autowired EntityManager entityManager;

    private MockMvc mvc;
    private User customer;
    private Product product;
    private String unique;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        unique = UUID.randomUUID().toString().substring(0, 12);
        customer = new User();
        customer.setFullName("Buyer " + unique);
        customer.setEmail(unique + "@example.test");
        customer.setPassword("test-only");
        customer.setRole(Role.CUSTOMER);
        entityManager.persist(customer);
        Category category = new Category();
        category.setName("Category " + unique);
        category.setSlug(unique);
        entityManager.persist(category);
        product = new Product();
        product.setCategory(category);
        product.setName("Product " + unique);
        product.setPrice(BigDecimal.valueOf(250));
        product.setStockQuantity(5);
        entityManager.persist(product);
    }

    @Test
    void guestAndCustomerCannotAccessStaffOrders() throws Exception {
        mvc.perform(get("/api/staff/orders")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/staff/orders/1").with(user("customer").roles("CUSTOMER")))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/staff/orders/1/items/1").with(user("customer").roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void listFiltersBothDayBoundariesAndOrdersByTimeThenId() throws Exception {
        Order first = order("START", "2026-11-17T00:00:00", OrderStatus.NEW);
        order("TIE1", "2026-11-18T23:59:59", OrderStatus.NEW);
        Order last = order("TIE2", "2026-11-18T23:59:59", OrderStatus.NEW);
        order("AFTER", "2026-11-19T00:00:00", OrderStatus.NEW);
        order("OTHER", "2026-11-17T12:00:00", OrderStatus.CONFIRMED);
        mvc.perform(get("/api/staff/orders").with(user("staff").roles("STAFF"))
                .param("keyword", "  BUYER " + unique.toUpperCase(java.util.Locale.ROOT) + "  ")
                .param("fromDate", "2026-11-17").param("toDate", "2026-11-18")
                .param("status", "NEW").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.content[0].id").value(last.getId()))
            .andExpect(jsonPath("$.content[0].orderDate").value("2026-11-18T23:59:59"));
        mvc.perform(get("/api/staff/orders").with(user("admin").roles("ADMIN"))
                .param("keyword", customer.getEmail()).param("fromDate", "2026-11-17")
                .param("toDate", "2026-11-18").param("status", "NEW").param("page", "1").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(first.getId()));
        mvc.perform(get("/api/staff/orders").with(user("staff").roles("STAFF"))
                .param("keyword", first.getOrderCode()).param("page", "2147483647"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void malformedFiltersIdsAndQuantitiesReturn400() throws Exception {
        mvc.perform(get("/api/staff/orders").with(user("staff").roles("STAFF")).param("size", "51"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/staff/orders").with(user("staff").roles("STAFF"))
                .param("fromDate", "2026-02-30"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/staff/orders").with(user("staff").roles("STAFF"))
                .param("fromDate", "2026-11-19").param("toDate", "2026-11-18"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/staff/orders/0").with(user("staff").roles("STAFF")))
            .andExpect(status().isBadRequest());
        for (String quantity : new String[] {"0", "1.5", "\"2\"", "null"}) {
            mvc.perform(put("/api/staff/orders/1/items/1").with(user("staff").roles("STAFF"))
                    .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":" + quantity + "}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }

    @Test
    void updateUsesPriceSnapshotAndRestocksEvenWhenProductIsHidden() throws Exception {
        Order order = order("EDIT", "2026-11-18T10:30:00", OrderStatus.NEW);
        long orderId = order.getId();
        long itemId = order.getItems().getFirst().getId();
        long productId = product.getId();
        mvc.perform(update(orderId, itemId, 4))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalAmount").value(400))
            .andExpect(jsonPath("$.items[0].unitPrice").value(100));
        Product refreshed = reloadProduct(productId);
        assertThat(refreshed.getStockQuantity()).isEqualTo(3);
        refreshed.setStatus(VisibilityStatus.HIDDEN);
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/staff/orders/{id}", orderId).with(user("staff").roles("STAFF")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].productId").value(productId))
            .andExpect(jsonPath("$.items[0].quantity").value(4));
        mvc.perform(update(orderId, itemId, 5))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("OUT_OF_STOCK"));
        mvc.perform(update(orderId, itemId, 1))
            .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].quantity").value(1))
            .andExpect(jsonPath("$.totalAmount").value(100));
        assertThat(reloadProduct(productId).getStockQuantity()).isEqualTo(6);
    }

    @Test
    void unrelatedLineAndFinalStatusCannotChangeStock() throws Exception {
        Order editable = order("EDIT", "2026-11-18T10:30:00", OrderStatus.CONFIRMED);
        Order completed = order("DONE", "2026-11-18T10:30:00", OrderStatus.COMPLETED);
        long completedItem = completed.getItems().getFirst().getId();
        mvc.perform(update(editable.getId(), completedItem, 3))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("NOT_FOUND"));
        mvc.perform(update(completed.getId(), completedItem, 3))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("ORDER_NOT_EDITABLE"));
        assertThat(reloadProduct(product.getId()).getStockQuantity()).isEqualTo(5);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder update(
            long orderId, long itemId, int quantity) {
        return put("/api/staff/orders/{id}/items/{itemId}", orderId, itemId)
            .with(user("admin").roles("ADMIN")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"quantity\":" + quantity + "}");
    }

    private Product reloadProduct(long productId) {
        entityManager.flush();
        entityManager.clear();
        return entityManager.find(Product.class, productId);
    }

    private Order order(String label, String date, OrderStatus status) {
        Order order = new Order();
        order.setUser(customer);
        order.setOrderCode("ORD-" + unique + "-" + label);
        order.setOrderDate(LocalDateTime.parse(date));
        order.setStatus(status);
        order.setReceiverName("Receiver");
        order.setReceiverPhone("0901234567");
        order.setShippingAddress("Example address");
        order.setPaymentMethod(PaymentMethod.COD);
        order.setTotalAmount(BigDecimal.valueOf(200));
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        order.getItems().add(item);
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }
}
