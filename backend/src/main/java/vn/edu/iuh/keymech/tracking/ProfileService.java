package vn.edu.iuh.keymech.tracking;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.config.AppProperties;
import vn.edu.iuh.keymech.dto.ActivitySignal;
import vn.edu.iuh.keymech.dto.InterestProfile;
import vn.edu.iuh.keymech.repository.UserActivityRepository;
import vn.edu.iuh.keymech.util.SessionKeys;

/**
 * Tổng hợp user_activity thành hồ sơ quan tâm để Chatbot cá nhân hóa gợi ý (mức Could, Sprint 3).
 * Chỉ đọc sự kiện 30 ngày gần nhất, tối đa 200 sự kiện mới nhất. Staff, Admin và khi tắt
 * {@code app.chatbot.personalization} thì luôn trả hồ sơ rỗng. Không bao giờ ném lỗi ra ngoài.
 *
 * <p>Trọng số: PURCHASE 3, ADD_TO_CART 2, VIEW_PRODUCT 1, SEARCH 1. Điểm bằng nhau thì thuộc tính
 * xuất hiện ở sự kiện gần hơn được ưu tiên.
 */
@Slf4j
@Service
public class ProfileService {

    static final int WINDOW_DAYS = 30;
    static final int MAX_EVENTS = 200;

    private final UserActivityRepository repository;
    private final AppProperties props;

    public ProfileService(UserActivityRepository repository, AppProperties props) {
        this.repository = repository;
        this.props = props;
    }

    /** Hồ sơ của người đang gọi: Customer theo user_id, Guest theo visitor_id trong Session. */
    @Transactional(readOnly = true)
    public InterestProfile getProfile(HttpSession session) {
        try {
            if (!props.chatbot().personalization() || session == null) {
                return InterestProfile.empty();
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (ActivityTracker.isStaffOrAdmin(auth)) {
                return InterestProfile.empty();
            }
            LocalDateTime since = LocalDateTime.now().minusDays(WINDOW_DAYS);
            PageRequest latest = PageRequest.of(0, MAX_EVENTS);

            Long userId = ActivityTracker.currentCustomerId(auth);
            List<ActivitySignal> signals;
            if (userId != null) {
                signals = repository.findSignalsByUser(userId, since, latest);
            } else if (session.getAttribute(SessionKeys.VISITOR_ID) instanceof String visitorId) {
                signals = repository.findSignalsByVisitor(visitorId, since, latest);
            } else {
                return InterestProfile.empty();   // chưa có hành vi nào, không tạo visitor_id chỉ để đọc
            }
            return InterestProfileBuilder.build(signals);
        } catch (Exception ex) {
            log.warn("Không dựng được hồ sơ quan tâm: {}", ex.toString());
            return InterestProfile.empty();
        }
    }
}
