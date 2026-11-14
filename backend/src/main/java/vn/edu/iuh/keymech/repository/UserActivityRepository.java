package vn.edu.iuh.keymech.repository;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.keymech.dto.ActivitySignal;
import vn.edu.iuh.keymech.model.UserActivity;
import vn.edu.iuh.keymech.model.enums.EventType;
=======
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.UserActivity;
>>>>>>> origin/main

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByVisitorIdOrderByCreatedAtDesc(String visitorId);
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId);
<<<<<<< HEAD

    /** Dùng để bỏ qua VIEW_PRODUCT lặp lại trong 60 giây của cùng một phiên. */
    boolean existsByVisitorIdAndEventTypeAndProductIdAndCreatedAtAfter(
        String visitorId, EventType eventType, Long productId, LocalDateTime after);

    /** Danh mục của sản phẩm, để điền category_id khi người gọi chỉ truyền product_id. */
    @Query("SELECT p.category.id FROM Product p WHERE p.id = :productId")
    Long findCategoryIdByProductId(@Param("productId") Long productId);

    /**
     * Gắn hành vi của khách vãng lai vào tài khoản khi đăng nhập (chỉ dòng chưa có user_id).
     * Trả về số dòng đã gắn.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserActivity a SET a.userId = :userId WHERE a.visitorId = :visitorId AND a.userId IS NULL")
    int claimVisitor(@Param("visitorId") String visitorId, @Param("userId") Long userId);

    /** Tín hiệu hành vi của một Customer (kèm thuộc tính sản phẩm), mới nhất trước. */
    @Query("""
        SELECT new vn.edu.iuh.keymech.dto.ActivitySignal(
            a.eventType, a.productId, p.name, p.layout, p.switchType, p.brand,
            COALESCE(p.category.id, a.categoryId), p.price, a.keyword, a.createdAt)
        FROM UserActivity a LEFT JOIN Product p ON p.id = a.productId
        WHERE a.userId = :userId AND a.createdAt >= :since
        ORDER BY a.createdAt DESC, a.id DESC
        """)
    List<ActivitySignal> findSignalsByUser(@Param("userId") Long userId,
                                           @Param("since") LocalDateTime since,
                                           Pageable pageable);

    /** Tín hiệu hành vi của một khách vãng lai theo visitor_id (chỉ dòng chưa gắn tài khoản). */
    @Query("""
        SELECT new vn.edu.iuh.keymech.dto.ActivitySignal(
            a.eventType, a.productId, p.name, p.layout, p.switchType, p.brand,
            COALESCE(p.category.id, a.categoryId), p.price, a.keyword, a.createdAt)
        FROM UserActivity a LEFT JOIN Product p ON p.id = a.productId
        WHERE a.visitorId = :visitorId AND a.userId IS NULL AND a.createdAt >= :since
        ORDER BY a.createdAt DESC, a.id DESC
        """)
    List<ActivitySignal> findSignalsByVisitor(@Param("visitorId") String visitorId,
                                              @Param("since") LocalDateTime since,
                                              Pageable pageable);
=======
>>>>>>> origin/main
}
