package vn.edu.iuh.keymech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
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
 * A-08: kiểm tra Dashboard bằng cách đối chiếu số đếm và doanh thu với dữ liệu
 * xác định trước. Cách đối chiếu: snapshot tổng trước khi tạo dữ liệu cố định,
 * rồi kiểm tra hiệu tăng (delta) sau khi thêm — không phụ thuộc seed data.
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
    void summaryCountsAndRevenueReflectFixedData() {
        DashboardResponse before = dashboardService.getSummary();

        // Dữ liệu cố định tạo trong cùng transaction (rollback sau test)
        User customer = createCustomer();
        Product dashProduct = createProduct(5, 1_000_000);
        createProduct(0, 2_000_000);

        Order paid = createOrder(customer, BigDecimal.valueOf(3_000_000),
                OrderStatus.SHIPPING, PaymentStatus.PAID);
        createOrder(customer, BigDecimal.valueOf(1_000_000), OrderStatus.NEW, PaymentStatus.UNPAID);

        // Item thuộc đơn paid (1 × 3.000.000) để tổng đơn khớp totalAmount
        OrderItem item = new OrderItem();
        item.setOrder(paid);
        item.setProduct(dashProduct);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.valueOf(3_000_000));
        paid.getItems().add(item);

        createCustomRequest(customer, CustomStatus.PENDING);
        createCustomRequest(customer, CustomStatus.IN_PROGRESS);

        DashboardResponse after = dashboardService.getSummary();

        // Số đếm tăng đúng bằng dữ liệu cố định
        assertEquals(before.totalOrders() + 2, after.totalOrders());
        assertEquals(before.pendingOrders() + 1, after.pendingOrders());
        assertEquals(before.totalProducts() + 2, after.totalProducts());
        assertEquals(before.totalUsers() + 1, after.totalUsers());
        assertEquals(before.customRequestsByStatus().get(CustomStatus.PENDING) + 1,
                after.customRequestsByStatus().get(CustomStatus.PENDING));
        assertEquals(before.customRequestsByStatus().get(CustomStatus.IN_PROGRESS) + 1,
                after.customRequestsByStatus().get(CustomStatus.IN_PROGRESS));
        assertEquals(before.customRequestsByStatus().get(CustomStatus.COMPLETED),
                after.customRequestsByStatus().get(CustomStatus.COMPLETED));

        // Số liệu "hôm nay" / "tháng này": đơn và khách đều tạo tại thời điểm chạy
        assertEquals(before.ordersToday() + 2, after.ordersToday());
        assertEquals(before.ordersThisMonth() + 2, after.ordersThisMonth());
        assertEquals(before.newUsersToday() + 1, after.newUsersToday());
        assertEquals(before.newUsersThisMonth() + 1, after.newUsersThisMonth());

        // Doanh thu: chỉ đơn PAID và chưa CANCELLED (đơn paid: 3.000.000)
        assertEquals(before.revenueToday().add(BigDecimal.valueOf(3_000_000)), after.revenueToday());
        assertEquals(before.revenueThisMonth().add(BigDecimal.valueOf(3_000_000)), after.revenueThisMonth());

        // Đối chiếu nội bộ: item của đơn paid khớp sản phẩm, số lượng và đơn giá
        assertEquals(dashProduct.getId(), item.getProduct().getId());
        assertEquals(1, item.getQuantity());
        assertEquals(paid.getTotalAmount(), item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
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
        String suffix = UUID.randomUUID().toString().substring(0, 16);
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
        String suffix = UUID.randomUUID().toString().substring(0, 16);
        CustomRequest request = new CustomRequest();
        request.setRequestCode("DASH-CR-" + suffix);
        request.setUser(user);
        request.setLayout("75%");
        request.setSwitchType("Linear");
        request.setStatus(status);
        return customRequests.save(request);
    }
}
