package vn.edu.iuh.keymech.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.keymech.model.UserActivity;
import vn.edu.iuh.keymech.model.enums.EventType;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByVisitorIdOrderByCreatedAtDesc(String visitorId);
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("update UserActivity a set a.userId = :userId where a.visitorId = :visitorId and a.userId is null")
    int attachGuestActivities(@Param("visitorId") String visitorId, @Param("userId") Long userId);

    @Query("""
        select a.eventType as eventType,
               coalesce(a.categoryId, c.id) as categoryId,
               coalesce(directCategory.name, c.name) as categoryName,
               p.layout as layout, p.switchType as switchType, p.brand as brand,
               p.name as productName, p.price as price, a.keyword as keyword,
               a.createdAt as createdAt
          from UserActivity a
          left join Product p on p.id = a.productId
          left join Category c on c.id = p.category.id
          left join Category directCategory on directCategory.id = a.categoryId
         where a.createdAt >= :since
           and ((:userId is not null and a.userId = :userId)
             or (:visitorId is not null and a.visitorId = :visitorId))
         order by a.createdAt desc
        """)
    List<ActivityProfileRow> findProfileRows(@Param("userId") Long userId,
                                              @Param("visitorId") String visitorId,
                                              @Param("since") LocalDateTime since);

    interface ActivityProfileRow {
        EventType getEventType();
        Long getCategoryId();
        String getCategoryName();
        String getLayout();
        String getSwitchType();
        String getBrand();
        String getProductName();
        java.math.BigDecimal getPrice();
        String getKeyword();
        LocalDateTime getCreatedAt();
    }
}
