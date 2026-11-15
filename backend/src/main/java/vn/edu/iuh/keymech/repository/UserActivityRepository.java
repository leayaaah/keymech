package vn.edu.iuh.keymech.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.UserActivity;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByVisitorIdOrderByCreatedAtDesc(String visitorId);
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
