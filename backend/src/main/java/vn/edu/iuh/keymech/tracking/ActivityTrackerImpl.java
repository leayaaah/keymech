package vn.edu.iuh.keymech.tracking;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
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
    private static final Duration VISITOR_LINK_TTL = Duration.ofMinutes(30);
    private final ActivityWriter activityWriter;
    private final ConcurrentMap<String, Instant> recentViews = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CompletableFuture<Void>> pendingByVisitor = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, VisitorLink> customerByVisitor = new ConcurrentHashMap<>();

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
            synchronized (pendingByVisitor) {
                Long resolvedUserId = userId != null ? userId : linkedCustomer(visitorId);
                enqueue(visitorId, () -> activityWriter.write(type, visitorId, resolvedUserId,
                    productId, categoryId, truncate(keyword, 100)));
            }
        } catch (Exception e) {
            log.warn("Could not schedule activity tracking ({})", type);
        }
    }

    @Override
    public void attachGuestActivities(String visitorId, Long userId) {
        if (visitorId == null || visitorId.isBlank() || userId == null) return;
        try {
            synchronized (pendingByVisitor) {
                customerByVisitor.put(visitorId, new VisitorLink(userId, Instant.now().plus(VISITOR_LINK_TTL)));
                cleanExpiredVisitorLinks();
                enqueue(visitorId, () -> activityWriter.attachGuestActivities(visitorId, userId));
            }
        } catch (Exception e) {
            log.warn("Could not schedule guest activity attachment");
        }
    }

    private Long linkedCustomer(String visitorId) {
        VisitorLink link = customerByVisitor.get(visitorId);
        if (link == null) return null;
        if (link.expiresAt().isBefore(Instant.now())) {
            customerByVisitor.remove(visitorId, link);
            return null;
        }
        return link.userId();
    }

    private void cleanExpiredVisitorLinks() {
        Instant now = Instant.now();
        customerByVisitor.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        if (customerByVisitor.size() > 10_000) {
            customerByVisitor.entrySet().stream()
                .min(Map.Entry.comparingByValue((first, second) -> first.expiresAt().compareTo(second.expiresAt())))
                .ifPresent(entry -> customerByVisitor.remove(entry.getKey(), entry.getValue()));
        }
    }

    /** Serializes writes and login attachment for one visitor so late async writes cannot remain Guest-only. */
    private void enqueue(String visitorId, Supplier<CompletableFuture<Void>> operation) {
        synchronized (pendingByVisitor) {
            CompletableFuture<Void> previous = pendingByVisitor.get(visitorId);
            CompletableFuture<Void> predecessor = previous == null
                ? CompletableFuture.completedFuture(null) : previous.handle((ignored, error) -> null);
            CompletableFuture<Void> next = predecessor.thenCompose(ignored -> operation.get())
                .exceptionally(error -> {
                    log.warn("Could not schedule visitor activity operation");
                    return null;
                });
            pendingByVisitor.put(visitorId, next);
            next.whenComplete((ignored, error) -> {
                synchronized (pendingByVisitor) {
                    pendingByVisitor.remove(visitorId, next);
                }
            });
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

    private record VisitorLink(Long userId, Instant expiresAt) {}
}
