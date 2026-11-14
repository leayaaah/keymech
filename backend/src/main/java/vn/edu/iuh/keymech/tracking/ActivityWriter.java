package vn.edu.iuh.keymech.tracking;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.model.UserActivity;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.repository.UserActivityRepository;

/**
 * Ghi user_activity ở luồng riêng. Tách khỏi {@link ActivityTracker} vì @Async chỉ có tác dụng
 * khi gọi qua proxy Spring, không có tác dụng khi một phương thức gọi phương thức khác trong cùng class.
 * Mọi lỗi chỉ ghi log, không ném ra ngoài để không ảnh hưởng luồng chính.
 */
@Slf4j
@Component
public class ActivityWriter {

    /** Cùng một sản phẩm được xem lại trong khoảng này của cùng phiên thì không ghi thêm. */
    static final long VIEW_DEDUP_SECONDS = 60;

    private final UserActivityRepository repository;

    public ActivityWriter(UserActivityRepository repository) {
        this.repository = repository;
    }

    // Không đặt @Transactional ở đây: mỗi lời gọi repository tự có transaction riêng, nên lỗi ghi
    // được bắt gọn bên dưới mà không để lại transaction bị đánh dấu rollback-only.
    @Async
    public void save(ActivityEvent e) {
        try {
            if (e.eventType() == EventType.VIEW_PRODUCT && e.productId() != null
                && repository.existsByVisitorIdAndEventTypeAndProductIdAndCreatedAtAfter(
                    e.visitorId(), e.eventType(), e.productId(),
                    LocalDateTime.now().minusSeconds(VIEW_DEDUP_SECONDS))) {
                return;
            }
            Long categoryId = e.categoryId();
            if (categoryId == null && e.productId() != null) {
                categoryId = repository.findCategoryIdByProductId(e.productId());
            }
            UserActivity a = new UserActivity();
            a.setVisitorId(e.visitorId());
            a.setUserId(e.userId());
            a.setEventType(e.eventType());
            a.setProductId(e.productId());
            a.setCategoryId(categoryId);
            a.setKeyword(e.keyword());
            repository.save(a);
        } catch (Exception ex) {
            // Không log keyword hay id người dùng; chỉ loại sự kiện và lý do
            log.warn("Không ghi được sự kiện {}: {}", e.eventType(), ex.toString());
        }
    }

    /** Gắn hành vi của visitor_id vào tài khoản; chạy đồng bộ để hồ sơ có ngay sau khi đăng nhập. */
    @Transactional
    public int claim(String visitorId, Long userId) {
        return repository.claimVisitor(visitorId, userId);
    }
}
