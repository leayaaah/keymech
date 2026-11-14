package vn.edu.iuh.keymech.tracking;

import jakarta.servlet.http.HttpSession;
import java.util.Collection;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.util.SessionKeys;

/**
 * Điểm duy nhất ghi hành vi người dùng (user tracking cho Chatbot). Quy ước mục 9:
 * <ul>
 *   <li>Bất đồng bộ, không ném lỗi ra ngoài: ghi hỏng thì request chính vẫn thành công.</li>
 *   <li>Bỏ qua Staff và Admin; chỉ ghi Guest (theo visitor_id) và Customer (kèm user_id).</li>
 *   <li>Không có endpoint nhận sự kiện từ client, không lưu IP hay User-Agent.</li>
 * </ul>
 *
 * <p><b>Cách gọi</b> (người gọi truyền {@code HttpSession} của request hiện tại):
 * <pre>
 *   tracker.recordView(session, productId);              // Thuận: GET /api/products/{id}
 *   tracker.recordSearch(session, keyword, categoryId);  // Thuận: GET /api/products?keyword=...
 *   tracker.recordAddToCart(session, productId);         // Thuận: POST /api/cart/items thành công
 *   tracker.recordPurchase(session, productIds);         // Thùy: SAU KHI placeOrder commit
 *   tracker.linkVisitorToUser(session, userId);          // Trí: khi Customer đăng nhập thành công
 * </pre>
 * Phải gọi từ luồng của request (cần SecurityContext và HttpSession), không gọi từ luồng @Async khác.
 */
@Slf4j
@Component
public class ActivityTracker {

    private final ActivityWriter writer;

    public ActivityTracker(ActivityWriter writer) {
        this.writer = writer;
    }

    /** Xem chi tiết sản phẩm (VIEW_PRODUCT). Cùng sản phẩm trong 60 giây của một phiên chỉ ghi một lần. */
    public void recordView(HttpSession session, Long productId) {
        if (productId == null) return;
        record(session, EventType.VIEW_PRODUCT, productId, null, null);
    }

    /** Tìm kiếm có từ khóa (SEARCH). Từ khóa rỗng thì bỏ qua; dài hơn 100 ký tự bị cắt. */
    public void recordSearch(HttpSession session, String keyword, Long categoryId) {
        String normalized = KeywordNormalizer.normalize(keyword);
        if (normalized == null) return;
        record(session, EventType.SEARCH, null, categoryId, normalized);
    }

    /** Thêm giỏ thành công (ADD_TO_CART). Chỉ ghi sự kiện, không lưu nội dung giỏ. */
    public void recordAddToCart(HttpSession session, Long productId) {
        if (productId == null) return;
        record(session, EventType.ADD_TO_CART, productId, null, null);
    }

    /** Mua hàng (PURCHASE), một sự kiện cho mỗi sản phẩm. Chỉ gọi sau khi transaction đặt hàng đã commit. */
    public void recordPurchase(HttpSession session, Collection<Long> productIds) {
        if (productIds == null) return;
        for (Long productId : productIds) {
            if (productId != null) {
                record(session, EventType.PURCHASE, productId, null, null);
            }
        }
    }

    /**
     * Gọi khi Customer đăng nhập thành công (sau changeSessionId): gắn các sự kiện của visitor_id
     * hiện tại vào tài khoản để hành vi trước khi đăng nhập được tính vào hồ sơ. Staff và Admin không gọi.
     */
    public void linkVisitorToUser(HttpSession session, Long userId) {
        try {
            if (session == null || userId == null) return;
            Object visitorId = session.getAttribute(SessionKeys.VISITOR_ID);
            if (visitorId instanceof String id) {
                int n = writer.claim(id, userId);
                log.debug("Đã gắn {} sự kiện của khách vãng lai vào tài khoản", n);
            }
        } catch (Exception ex) {
            log.warn("Không gắn được hành vi vào tài khoản: {}", ex.toString());
        }
    }

    /** visitor_id của phiên (tạo nếu chưa có). Public để ProfileService đọc hồ sơ của Guest. */
    public String visitorId(HttpSession session) {
        Object existing = session.getAttribute(SessionKeys.VISITOR_ID);
        if (existing instanceof String id) return id;
        synchronized (WebUtils.getSessionMutex(session)) {
            Object again = session.getAttribute(SessionKeys.VISITOR_ID);
            if (again instanceof String id) return id;
            String created = UUID.randomUUID().toString();
            session.setAttribute(SessionKeys.VISITOR_ID, created);
            return created;
        }
    }

    // ------------------------------------------------------------------

    private void record(HttpSession session, EventType type, Long productId, Long categoryId, String keyword) {
        try {
            if (session == null) return;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (isStaffOrAdmin(auth)) return;   // Staff và Admin không bị theo dõi
            Long userId = currentCustomerId(auth);
            writer.save(new ActivityEvent(visitorId(session), userId, type, productId, categoryId, keyword));
        } catch (Exception ex) {
            log.warn("Không ghi được sự kiện {}: {}", type, ex.toString());
        }
    }

    static boolean isStaffOrAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
            .anyMatch(g -> "ROLE_STAFF".equals(g.getAuthority()) || "ROLE_ADMIN".equals(g.getAuthority()));
    }

    static Long currentCustomerId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getPrincipal() instanceof AppUserDetails u ? u.getId() : null;
    }
}
