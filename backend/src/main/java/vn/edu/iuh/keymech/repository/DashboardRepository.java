package vn.edu.iuh.keymech.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import vn.edu.iuh.keymech.model.Order;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

public interface DashboardRepository extends Repository<Order, Long> {

    @Query("select count(o) from Order o")
    long countOrders();

    @Query("select count(o) from Order o where o.status = NEW")
    long countPendingOrders();

    @Query("select count(o) from Order o where o.orderDate >= :start and o.orderDate < :end")
    long countOrdersBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            select coalesce(sum(o.totalAmount), 0) from Order o
            where o.paymentStatus = PAID and o.status <> CANCELLED
              and o.orderDate >= :start and o.orderDate < :end
            """)
    BigDecimal revenueBetween(LocalDateTime start, LocalDateTime end);

    @Query("select count(u) from User u")
    long countUsers();

    @Query("select count(u) from User u where u.createdAt >= :start and u.createdAt < :end")
    long countUsersBetween(LocalDateTime start, LocalDateTime end);

    @Query("select count(p) from Product p")
    long countProducts();

    @Query("select r.status as status, count(r) as total from CustomRequest r group by r.status")
    List<CustomCount> countCustomByStatus();

    interface CustomCount {
        CustomStatus getStatus();
        long getTotal();
    }
}
