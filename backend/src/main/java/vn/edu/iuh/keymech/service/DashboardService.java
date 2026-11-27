package vn.edu.iuh.keymech.service;

import java.time.LocalDate;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.DashboardResponse;
import vn.edu.iuh.keymech.model.enums.CustomStatus;
import vn.edu.iuh.keymech.repository.DashboardRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboard;

    @Transactional(readOnly = true)
    public DashboardResponse getSummary() {
        LocalDate today = LocalDate.now();
        var dayStart = today.atStartOfDay();
        var nextDay = today.plusDays(1).atStartOfDay();
        var monthStart = today.withDayOfMonth(1).atStartOfDay();
        var nextMonth = monthStart.plusMonths(1);
        var customCounts = new EnumMap<CustomStatus, Long>(CustomStatus.class);
        for (CustomStatus status : CustomStatus.values()) {
            customCounts.put(status, 0L);
        }
        dashboard.countCustomByStatus().forEach(count -> customCounts.put(count.getStatus(), count.getTotal()));
        return new DashboardResponse(
                dashboard.countOrders(), dashboard.countPendingOrders(), dashboard.countProducts(),
                customCounts.get(CustomStatus.PENDING), dashboard.countUsers(),
                dashboard.countOrdersBetween(dayStart, nextDay), dashboard.countOrdersBetween(monthStart, nextMonth),
                dashboard.revenueBetween(dayStart, nextDay), dashboard.revenueBetween(monthStart, nextMonth),
                dashboard.countUsersBetween(dayStart, nextDay), dashboard.countUsersBetween(monthStart, nextMonth),
                customCounts);
    }
}
