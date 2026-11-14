package vn.edu.iuh.keymech.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.iuh.keymech.config.AppProperties;
import vn.edu.iuh.keymech.dto.ActivitySignal;
import vn.edu.iuh.keymech.dto.InterestProfile;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.UserActivityRepository;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.util.SessionKeys;

class ProfileServiceTest {

    private final UserActivityRepository repository = mock(UserActivityRepository.class);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private ProfileService service(boolean personalization) {
        return new ProfileService(repository,
            new AppProperties("http://localhost:5173", "./uploads",
                new AppProperties.Chatbot("rule", personalization, 8, 10, 200)));
    }

    private static void loginAs(long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail("u" + id + "@example.com");
        u.setPassword("x");
        u.setRole(role);
        AppUserDetails p = new AppUserDetails(u);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));
    }

    private static ActivitySignal view() {
        return new ActivitySignal(EventType.VIEW_PRODUCT, 1L, "Keychron K2", "75%", "Tactile", "Keychron",
            1L, new BigDecimal("2200000"), null, LocalDateTime.now());
    }

    @Test
    @DisplayName("TC-BOT-17: Customer đã xem phím 75% thì hồ sơ nghiêng về 75% và hãng đó")
    void getProfile_customer_usesUserId() {
        loginAs(7L, Role.CUSTOMER);
        when(repository.findSignalsByUser(any(), any(LocalDateTime.class), any(Pageable.class)))
            .thenReturn(List.of(view(), view()));

        InterestProfile p = service(true).getProfile(new MockHttpSession());

        assertThat(p.topLayout()).isEqualTo("75%");
        assertThat(p.topBrand()).isEqualTo("Keychron");
        assertThat(p.recentProducts()).hasSize(1);
    }

    @Test
    @DisplayName("TC-BOT-18: Guest chưa có hành vi nào thì hồ sơ rỗng, không lỗi và không tạo visitor_id")
    void getProfile_guestWithoutVisitorId_isEmpty() {
        MockHttpSession session = new MockHttpSession();

        InterestProfile p = service(true).getProfile(session);

        assertThat(p.isEmpty()).isTrue();
        assertThat(session.getAttribute(SessionKeys.VISITOR_ID)).isNull();
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Guest đã có visitor_id thì đọc theo visitor_id")
    void getProfile_guestWithVisitorId_readsByVisitor() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.VISITOR_ID, "abc");
        when(repository.findSignalsByVisitor(anyString(), any(LocalDateTime.class), any(Pageable.class)))
            .thenReturn(List.of(view()));

        assertThat(service(true).getProfile(session).topLayout()).isEqualTo("75%");
    }

    @Test
    @DisplayName("TC-TRK-03: Staff và Admin luôn nhận hồ sơ rỗng")
    void getProfile_staff_isEmpty() {
        loginAs(20L, Role.STAFF);

        assertThat(service(true).getProfile(new MockHttpSession()).isEmpty()).isTrue();
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Tắt personalization thì không đọc CSDL")
    void getProfile_personalizationOff_isEmpty() {
        loginAs(7L, Role.CUSTOMER);

        assertThat(service(false).getProfile(new MockHttpSession()).isEmpty()).isTrue();
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Lỗi CSDL thì trả hồ sơ rỗng, Chatbot vẫn chạy")
    void getProfile_repositoryFails_isEmpty() {
        loginAs(7L, Role.CUSTOMER);
        when(repository.findSignalsByUser(any(), any(LocalDateTime.class), any(Pageable.class)))
            .thenThrow(new RuntimeException("DB down"));

        assertThatCode(() -> service(true).getProfile(new MockHttpSession())).doesNotThrowAnyException();
        assertThat(service(true).getProfile(new MockHttpSession()).isEmpty()).isTrue();
    }
}
