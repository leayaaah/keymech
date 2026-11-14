package vn.edu.iuh.keymech.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.util.SessionKeys;

@ExtendWith(MockitoExtension.class)
class ActivityTrackerTest {

    @Mock ActivityWriter writer;
    @InjectMocks ActivityTracker tracker;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static void loginAs(long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail("u" + id + "@example.com");
        u.setPassword("x");
        u.setRole(role);
        AppUserDetails principal = new AppUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private static void asAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    }

    private ActivityEvent onlyEvent() {
        ArgumentCaptor<ActivityEvent> c = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(writer).save(c.capture());
        return c.getValue();
    }

    @Test
    @DisplayName("TC-TRK-01: Guest xem sản phẩm ghi VIEW_PRODUCT với visitor_id và user_id NULL")
    void recordView_guest_hasVisitorIdAndNoUser() {
        asAnonymous();
        MockHttpSession session = new MockHttpSession();

        tracker.recordView(session, 5L);

        ActivityEvent e = onlyEvent();
        assertThat(e.eventType()).isEqualTo(EventType.VIEW_PRODUCT);
        assertThat(e.productId()).isEqualTo(5L);
        assertThat(e.userId()).isNull();
        assertThat(UUID.fromString(e.visitorId())).isNotNull();                    // đúng dạng UUID
        assertThat(session.getAttribute(SessionKeys.VISITOR_ID)).isEqualTo(e.visitorId());
    }

    @Test
    @DisplayName("TC-TRK-01: chưa có Authentication nào (Guest thật) vẫn ghi được")
    void recordView_noAuthentication_treatedAsGuest() {
        tracker.recordView(new MockHttpSession(), 5L);

        assertThat(onlyEvent().userId()).isNull();
    }

    @Test
    @DisplayName("Customer đăng nhập thì sự kiện mang user_id")
    void recordAddToCart_customer_hasUserId() {
        loginAs(7L, Role.CUSTOMER);

        tracker.recordAddToCart(new MockHttpSession(), 3L);

        ActivityEvent e = onlyEvent();
        assertThat(e.eventType()).isEqualTo(EventType.ADD_TO_CART);
        assertThat(e.userId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("TC-TRK-03: Staff xem sản phẩm và tìm kiếm không ghi dòng nào")
    void staff_isNeverTracked() {
        loginAs(20L, Role.STAFF);
        MockHttpSession session = new MockHttpSession();

        tracker.recordView(session, 5L);
        tracker.recordSearch(session, "keychron", null);
        tracker.recordAddToCart(session, 5L);
        tracker.recordPurchase(session, List.of(5L));

        verifyNoInteractions(writer);
    }

    @Test
    @DisplayName("TC-TRK-03: Admin cũng không bị ghi")
    void admin_isNeverTracked() {
        loginAs(1L, Role.ADMIN);

        tracker.recordView(new MockHttpSession(), 5L);
        tracker.recordSearch(new MockHttpSession(), "keychron", 1L);

        verifyNoInteractions(writer);
    }

    @Test
    @DisplayName("visitor_id ổn định trong cùng một phiên")
    void visitorId_isStableWithinSession() {
        MockHttpSession session = new MockHttpSession();

        String first = tracker.visitorId(session);
        String second = tracker.visitorId(session);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("SEARCH: từ khóa được chuẩn hóa và cắt còn 100 ký tự")
    void recordSearch_normalizesKeyword() {
        tracker.recordSearch(new MockHttpSession(), "  phím   75%  " + "x".repeat(200), 2L);

        ActivityEvent e = onlyEvent();
        assertThat(e.eventType()).isEqualTo(EventType.SEARCH);
        assertThat(e.keyword()).hasSize(100).startsWith("phím 75% x");
        assertThat(e.categoryId()).isEqualTo(2L);
        assertThat(e.productId()).isNull();
    }

    @Test
    @DisplayName("SEARCH: từ khóa rỗng hoặc null thì bỏ qua")
    void recordSearch_blankKeyword_ignored() {
        tracker.recordSearch(new MockHttpSession(), "   ", null);
        tracker.recordSearch(new MockHttpSession(), null, null);

        verifyNoInteractions(writer);
    }

    @Test
    @DisplayName("PURCHASE: một sự kiện cho mỗi sản phẩm, bỏ qua id null")
    void recordPurchase_oneEventPerProduct() {
        loginAs(7L, Role.CUSTOMER);

        tracker.recordPurchase(new MockHttpSession(), Arrays.asList(1L, null, 2L, 3L));

        ArgumentCaptor<ActivityEvent> c = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(writer, times(3)).save(c.capture());
        assertThat(c.getAllValues()).extracting(ActivityEvent::eventType).containsOnly(EventType.PURCHASE);
        assertThat(c.getAllValues()).extracting(ActivityEvent::productId).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("TC-TRK-05: ghi lỗi thì request chính vẫn thành công (không ném ngoại lệ)")
    void record_writerFails_doesNotThrow() {
        doThrow(new RuntimeException("DB down")).when(writer).save(any());

        assertThatCode(() -> tracker.recordView(new MockHttpSession(), 5L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Không có session hoặc thiếu id thì bỏ qua, không lỗi")
    void record_nullArguments_ignored() {
        assertThatCode(() -> {
            tracker.recordView(null, 5L);
            tracker.recordView(new MockHttpSession(), null);
            tracker.recordAddToCart(new MockHttpSession(), null);
            tracker.recordPurchase(new MockHttpSession(), null);
        }).doesNotThrowAnyException();

        verifyNoInteractions(writer);
    }

    @Test
    @DisplayName("TC-TRK-02: đăng nhập thì gắn các dòng của visitor_id vào user_id")
    void linkVisitorToUser_claimsActivity() {
        MockHttpSession session = new MockHttpSession();
        String visitorId = tracker.visitorId(session);

        tracker.linkVisitorToUser(session, 7L);

        verify(writer).claim(visitorId, 7L);
    }

    @Test
    @DisplayName("TC-TRK-02: chưa có visitor_id (chưa xem gì) thì không gắn gì")
    void linkVisitorToUser_withoutVisitorId_doesNothing() {
        tracker.linkVisitorToUser(new MockHttpSession(), 7L);

        verify(writer, never()).claim(anyString(), anyLong());
    }

    @Test
    @DisplayName("TC-TRK-02: gắn lỗi thì đăng nhập vẫn không bị ảnh hưởng")
    void linkVisitorToUser_writerFails_doesNotThrow() {
        MockHttpSession session = new MockHttpSession();
        tracker.visitorId(session);
        when(writer.claim(anyString(), anyLong())).thenThrow(new RuntimeException("DB down"));

        assertThatCode(() -> tracker.linkVisitorToUser(session, 7L)).doesNotThrowAnyException();
    }
}
