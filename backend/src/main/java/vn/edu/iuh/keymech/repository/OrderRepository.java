package vn.edu.iuh.keymech.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.keymech.model.Order;
import vn.edu.iuh.keymech.model.enums.OrderStatus;

// Chỉ công khai các thao tác cần dùng; orders không được xóa vật lý.
public interface OrderRepository extends Repository<Order, Long> {
    <S extends Order> S save(S order);
    Optional<Order> findById(Long id);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    Optional<Order> findByOrderCode(String orderCode);

    String STAFF_FILTER = """
        WHERE (:keyword IS NULL OR LOCATE(LOWER(:keyword), LOWER(o.orderCode)) > 0
            OR LOCATE(LOWER(:keyword), LOWER(u.fullName)) > 0
            OR LOCATE(LOWER(:keyword), LOWER(u.email)) > 0)
          AND (:fromTime IS NULL OR o.orderDate >= :fromTime)
          AND (:toTime IS NULL OR o.orderDate <= :toTime)
          AND (:status IS NULL OR o.status = :status)
        """;

    String STAFF_COUNT = "SELECT COUNT(o) FROM Order o JOIN o.user u " + STAFF_FILTER;

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user u " + STAFF_FILTER + "ORDER BY o.orderDate DESC, o.id DESC",
           countQuery = STAFF_COUNT)
    Page<Order> findStaffOrders(@Param("keyword") String keyword, @Param("fromTime") LocalDateTime fromTime,
                                @Param("toTime") LocalDateTime toTime, @Param("status") OrderStatus status,
                                Pageable pageable);

    @Query(STAFF_COUNT)
    long countStaffOrders(@Param("keyword") String keyword, @Param("fromTime") LocalDateTime fromTime,
                          @Param("toTime") LocalDateTime toTime, @Param("status") OrderStatus status);

    @EntityGraph(attributePaths = {"user", "items.product"})
    Optional<Order> findStaffDetailById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findLockedById(@Param("id") Long id);
}
