package vn.edu.iuh.keymech.tracking;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.keymech.model.enums.EventType;

/** Request-facing facade: validates events, deduplicates views, and schedules persistence. */
@Slf4j
@Service
public class ActivityTrackerImpl implements ActivityTracker {
    private static final Duration VIEW_DEDUP_WINDOW = Duration.ofSeconds(60);
    private final ActivityWriter activityWriter;
    private final ConcurrentMap<String, Instant> recentViews = new ConcurrentHashMap<>();

    public ActivityTrackerImpl(ActivityWriter activityWriter) {
        this.activityWriter = activityWriter;
    }

    @Override
    public void record(EventType type, String visitorId, Long userId,
                       Long productId, Long categoryId, String keyword) {
        try {
            if (type == null || visitorId == null || visitorId.isBlank()) return;
            if (type == EventType.VIEW_PRODUCT && (productId == null || isDuplicateView(visitorId, productId))) return;
            if (type == EventType.SEARCH && (keyword == null || keyword.isBlank())) return;
            activityWriter.write(type, visitorId, userId, productId, categoryId, truncate(keyword, 100));
        } catch (Exception e) {
            log.warn("Could not schedule activity tracking ({})", type);
        }
    }

    @Override
    public void attachGuestActivities(String visitorId, Long userId) {
        if (visitorId == null || visitorId.isBlank() || userId == null) return;
        try {
            activityWriter.attachGuestActivities(visitorId, userId);
        } catch (Exception e) {
            log.warn("Could not schedule guest activity attachment");
        }
    }

    private boolean isDuplicateView(String visitorId, Long productId) {
        Instant now = Instant.now();
        String key = visitorId + ":" + productId;
        Instant previous = recentViews.put(key, now);
        if (recentViews.size() > 10_000) {
            Instant cutoff = now.minus(VIEW_DEDUP_WINDOW);
            recentViews.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        }
        if (previous != null && previous.plus(VIEW_DEDUP_WINDOW).isAfter(now)) {
            recentViews.put(key, previous);
            return true;
        }
        return false;
    }

    private static String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
