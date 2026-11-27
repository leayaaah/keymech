package vn.edu.iuh.keymech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.DashboardResponse;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.CustomRequest;
import vn.edu.iuh.keymech.model.Order;
import vn.edu.iuh.keymech.model.OrderItem;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.CustomStatus;
import vn.edu.iuh.keymech.model.enums.OrderStatus;
import vn.edu.iuh.keymech.model.enums.PaymentMethod;
import vn.edu.iuh.keymech.model.enums.PaymentStatus;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.CategoryRepository;
import vn.edu.iuh.keymech.repository.CustomRequestRepository;
import vn.edu.iuh.keymech.repository.OrderRepository;
import vn.edu.iuh.keymech.repository.ProductRepository;
import vn.edu.iuh.keymech.repository.UserRepository;

/**
 * A-08: kiểm tra đối chiếu số đếm và doanh thu Dashboard với dữ liệu cố định.
 * Dữ liệu tạo trong cùng transaction (rollback sau test), nên số liệu "hôm nay/tháng này"
 * được đếm chính xác dựa trên mốc thời gian tại thời điểm chạy test.
 */
@SpringBootTest
@Transactional
class DashboardServiceTest {

    @Autowired DashboardService dashboardService;
    @Autowired UserRepository users;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired CustomRequestRepository customRequests;

    @Test
    void countsAndRevenueMatchFixedData() {
        // Dữ liệu cố định tạo ngay khi chạy test
        User customer = createCustomer();
        Product dashProduct = createProduct(5, 1_000_000);
        createProduct(0, 2_000_000);

        Order paid = createOrder(customer, BigDecimal.valueOf(3_000_000),
                OrderStatus.SHIPPING, PaymentStatus.PAID);
        createOrder(customer, BigDecimal.valueOf(1_000_000), OrderStatus.NEW, PaymentStatus.UNPAID);

        // Item thuộc đơn paid (1 sản phẩm × 3.000.000) để tổng đơn khớp totalAmount
        OrderItem item = new OrderItem();
        item.setOrder(paid);
        item.setProduct(dashProduct);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.valueOf(3_000_000));
        paid.getItems().add(item);

        createCustomRequest(customer, CustomStatus.PENDING);
        createCustomRequest(customer, CustomStatus.IN_PROGRESS);

        DashboardResponse summary = dashboardService.getSummary();

        // Số đếm tối thiểu khớp dữ liệu cố định (DB cô lập cho test)
        assertEquals(2, summary.totalOrders());
        assertEquals(1, summary.pendingOrders());
        assertEquals(2, summary.totalProducts());
        assertEquals(1, summary.customRequestsByStatus().get(CustomStatus.PENDING));
        assertEquals(1, summary.customRequestsByStatus().get(CustomStatus.IN_PROGRESS));
        assertEquals(0L, summary.customRequestsByStatus().get(CustomStatus.COMPLETED));
        assertEquals(1, summary.totalUsers());

        // Số liệu "hôm nay" và "tháng này" — cả hai đơn + khách tạo ngay tại thời điểm chạy
        assertEquals(2, summary.ordersToday());
        assertEquals(2, summary.ordersThisMonth());
        assertEquals(1, summary.newUsersToday());
        assertEquals(1, summary.newUsersThisMonth());

        // Doanh thu chỉ tính đơn PAID và chưa CANCELLED: chỉ đơn "paid" (3.000.000)
        assertEquals(BigDecimal.valueOf(3_000_000), summary.revenueToday());
        assertEquals(BigDecimal.valueOf(3_000_000), summary.revenueThisMonth());

        // Kiểm tra đối chiếu nội bộ: item của đơn paid khớp sản phẩm, số lượng và đơn giá
        List<OrderItem> items = paid.getItems();
        assertEquals(1, items.size());
        assertEquals(1, items.getFirst().getQuantity());
        assertEquals(dashProduct.getId(), items.getFirst().getProduct().getId());
        assertEquals(2, summary.totalProducts());
        assertTrue(summary.customRequestsByStatus().values().stream()
                .allMatch(count -> count >= 0));
    }

    private User createCustomer() {
        String suffix = UUID.randomUUID().toString();
        User user = new User();
        user.setFullName("Dashboard test " + suffix);
        user.setEmail("dashboard-" + suffix + "@example.test");
        user.setPassword("test-only");
        user.setRole(Role.CUSTOMER);
        return users.save(user);
    }

    private Product createProduct(int stock, long price) {
        String suffix = UUID.randomUUID().toString();
        Category category = new Category();
        category.setName("Dashboard test " + suffix);
        category.setSlug("dashboard-test-" + suffix);
        categories.save(category);

        Product product = new Product();
        product.setCategory(category);
        product.setName("Dashboard product " + suffix);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStockQuantity(stock);
        return products.save(product);
    }

    private Order createOrder(User user, BigDecimal total, OrderStatus status, PaymentStatus payment) {
        String suffix = UUID.randomUUID().toString();
        Order order = new Order();
        order.setOrderCode("DASH-" + suffix);
        order.setUser(user);
        order.setReceiverName("Dashboard receiver");
        order.setReceiverPhone("0901234567");
        order.setShippingAddress("Test address");
        order.setPaymentMethod(PaymentMethod.COD);
        order.setPaymentStatus(payment);
        order.setStatus(status);
        order.setTotalAmount(total);
        return orders.save(order);
    }

    private CustomRequest createCustomRequest(User user, CustomStatus status) {
        String suffix = UUID.randomUUID().toString();
        CustomRequest request = new CustomRequest();
        request.setRequestCode("DASH-CR-" + suffix);
        request.setUser(user);
        request.setLayout("75%");
        request.setSwitchType("Linear");
        request.setStatus(status);
        return customRequests.save(request);
    }
}
