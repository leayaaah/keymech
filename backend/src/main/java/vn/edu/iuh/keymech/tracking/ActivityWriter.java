package vn.edu.iuh.keymech.tracking;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.UserActivity;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserActivityRepository;
import vn.edu.iuh.keymech.repository.UserRepository;

/** Persists tracking data asynchronously so persistence failures never affect a customer request. */
@Slf4j
@Service
public class ActivityWriter {
    private final UserActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityWriter(UserActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @Async
    @Transactional
    public void write(EventType type, String visitorId, Long userId, Long productId,
                      Long categoryId, String keyword) {
        try {
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null || user.getRole() != Role.CUSTOMER) return;
            }
            UserActivity activity = new UserActivity();
            activity.setVisitorId(visitorId);
            activity.setUserId(userId);
            activity.setEventType(type);
            activity.setProductId(productId);
            activity.setCategoryId(categoryId);
            activity.setKeyword(keyword);
            activity.setCreatedAt(LocalDateTime.now());
            activityRepository.save(activity);
        } catch (Exception e) {
            log.warn("Could not persist user activity ({})", e.getClass().getSimpleName());
        }
    }

    @Async
    @Transactional
    public void attachGuestActivities(String visitorId, Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getRole() != Role.CUSTOMER) return;
            activityRepository.attachGuestActivities(visitorId, userId);
        } catch (Exception e) {
            log.warn("Could not attach guest activities ({})", e.getClass().getSimpleName());
        }
    }
}
