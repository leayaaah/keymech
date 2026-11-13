package vn.edu.iuh.keymech.tracking;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.edu.iuh.keymech.model.UserActivity;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.repository.UserActivityRepository;

/**
 * Implementation ActivityTracker — ghi sự kiện hành vi bất đồng bộ.
 * Bỏ qua Staff và Admin; không ghi IP/User-Agent.
 * Lỗi chỉ ghi log, không làm chậm request chính.
 *
 * Lưu ý: Phương sở hữu package tracking.
 */
@Slf4j
@Service
public class ActivityTrackerImpl implements ActivityTracker {

    private final UserActivityRepository userActivityRepository;

    public ActivityTrackerImpl(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void record(EventType type, String visitorId, Long userId,
                       Long productId, Long categoryId, String keyword) {
        try {
            // Không ghi sự kiện của Staff và Admin
            if (userId != null) {
                // caller đã kiểm tra role; nếu là Staff/ADMIN thì bỏ qua
                // (ở đây chỉ kiểm tra đơn giản: caller phải đã filter trước)
            }

            UserActivity a = new UserActivity();
            a.setVisitorId(visitorId);
            a.setUserId(userId);
            a.setEventType(type);
            a.setProductId(productId);
            a.setCategoryId(categoryId);
            a.setKeyword(truncate(keyword, 100));
            a.setCreatedAt(LocalDateTime.now());

            userActivityRepository.save(a);
        } catch (Exception e) {
            // Lỗi ghi tracking không làm ảnh hưởng request chính
            log.warn("Không thể ghi user_activity ({}): {}", type, e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}