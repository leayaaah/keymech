package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import java.util.Map;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

public record DashboardResponse(
        long totalOrders, long pendingOrders, long totalProducts, long pendingCustomRequests, long totalUsers,
        long ordersToday, long ordersThisMonth, BigDecimal revenueToday, BigDecimal revenueThisMonth,
        long newUsersToday, long newUsersThisMonth, Map<CustomStatus, Long> customRequestsByStatus) {}
