package vn.edu.iuh.keymech.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import vn.edu.iuh.keymech.dto.CreateCustomRequest;
import vn.edu.iuh.keymech.dto.CustomRequestDetailResponse;
import vn.edu.iuh.keymech.dto.CustomRequestResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.CustomRequest;
import vn.edu.iuh.keymech.model.CustomRequestHistory;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.CustomStatus;
import vn.edu.iuh.keymech.model.enums.Role;
import vn.edu.iuh.keymech.repository.CustomRequestHistoryRepository;
import vn.edu.iuh.keymech.repository.CustomRequestRepository;
import vn.edu.iuh.keymech.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomRequestServiceTest {

    private static final long CUSTOMER_ID = 10L;
    private static final long OTHER_CUSTOMER_ID = 11L;
    private static final long STAFF_ID = 20L;
    private static final long ADMIN_ID = 30L;

    @Mock CustomRequestRepository requests;
    @Mock CustomRequestHistoryRepository histories;
    @Mock UserRepository users;
    @InjectMocks CustomRequestService service;

    // ---------- dữ liệu mẫu ----------

    private static User user(long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setFullName("Người dùng " + id);
        u.setEmail("u" + id + "@example.com");
        u.setRole(role);
        return u;
    }

    private static CustomRequest request(long id, long ownerId, CustomStatus status, BigDecimal quoted) {
        CustomRequest r = new CustomRequest();
        r.setId(id);
        r.setRequestCode("CUS-TEST" + id);
        r.setUser(user(ownerId, Role.CUSTOMER));
        r.setLayout("75%");
        r.setSwitchType("Linear");
        r.setStatus(status);
        r.setQuotedPrice(quoted);
        return r;
    }

    private void givenLocked(CustomRequest r) {
        when(requests.findByIdForUpdate(r.getId())).thenReturn(Optional.of(r));
    }

    private CustomRequestHistory savedHistory() {
        ArgumentCaptor<CustomRequestHistory> c = ArgumentCaptor.forClass(CustomRequestHistory.class);
        verify(histories).save(c.capture());
        return c.getValue();
    }

    private static CreateCustomRequest form(Boolean hotSwap, String note) {
        return new CreateCustomRequest("75%", " Linear - Gateron Yellow ", "Nhôm", null, null, hotSwap,
            "RGB", new BigDecimal("3000000"), note);
    }

    // ---------- C-03: gửi yêu cầu ----------

    @Test
    @DisplayName("C-03: gửi yêu cầu tạo PENDING, mã CUS-, kèm dòng lịch sử đầu tiên không có old_status")
    void create_savesPendingAndInitialHistory() {
        User customer = user(CUSTOMER_ID, Role.CUSTOMER);
        when(users.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        CustomRequestResponse res = service.create(CUSTOMER_ID, form(null, "  "));

        assertThat(res.status()).isEqualTo(CustomStatus.PENDING);
        assertThat(res.requestCode()).startsWith("CUS-");
        assertThat(res.customer()).isNull();                 // phía khách không lộ khối customer
        assertThat(res.switchType()).isEqualTo("Linear - Gateron Yellow");   // đã trim
        assertThat(res.hotSwap()).isTrue();                  // mặc định có hot-swap
        assertThat(res.lubing()).isFalse();                  // mặc định không lube
        assertThat(res.note()).isNull();                     // ghi chú toàn khoảng trắng thành null

        CustomRequestHistory h = savedHistory();
        assertThat(h.getOldStatus()).isNull();
        assertThat(h.getNewStatus()).isEqualTo(CustomStatus.PENDING);
        assertThat(h.getChangedBy()).isSameAs(customer);
    }

    @Test
    @DisplayName("C-03: hotSwap=false được giữ nguyên, không bị ghi đè bằng mặc định")
    void create_respectsExplicitHotSwapFalse() {
        when(users.findById(CUSTOMER_ID)).thenReturn(Optional.of(user(CUSTOMER_ID, Role.CUSTOMER)));
        assertThat(service.create(CUSTOMER_ID, form(false, null)).hotSwap()).isFalse();
    }

    // ---------- C-04: xem của mình ----------

    @Test
    @DisplayName("TC-CUS-09: khách xem yêu cầu của người khác bị từ chối (403)")
    void getMy_otherOwner_throwsAccessDenied() {
        when(requests.findById(1L)).thenReturn(Optional.of(request(1L, OTHER_CUSTOMER_ID, CustomStatus.PENDING, null)));

        assertThatThrownBy(() -> service.getMy(1L, CUSTOMER_ID)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("C-04: id không tồn tại trả 404")
    void getMy_missing_throwsNotFound() {
        when(requests.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMy(99L, CUSTOMER_ID)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("C-04: timeline phía khách chỉ ghi Bạn hoặc Cửa hàng, không lộ tên Staff")
    void getMy_timelineHidesStaffName() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.QUOTED, new BigDecimal("2500000"));
        when(requests.findById(1L)).thenReturn(Optional.of(r));
        CustomRequestHistory byCustomer = history(r, null, CustomStatus.PENDING, user(CUSTOMER_ID, Role.CUSTOMER));
        CustomRequestHistory byStaff = history(r, CustomStatus.PENDING, CustomStatus.QUOTED, user(STAFF_ID, Role.STAFF));
        when(histories.findTimeline(1L)).thenReturn(List.of(byCustomer, byStaff));

        CustomRequestDetailResponse d = service.getMy(1L, CUSTOMER_ID);

        assertThat(d.history()).extracting("actor").containsExactly("Bạn", "Cửa hàng");
        assertThat(d.allowedNext()).containsExactlyInAnyOrder(CustomStatus.ACCEPTED, CustomStatus.CANCELLED);
    }

    private static CustomRequestHistory history(CustomRequest r, CustomStatus from, CustomStatus to, User by) {
        CustomRequestHistory h = new CustomRequestHistory();
        h.setCustomRequest(r);
        h.setOldStatus(from);
        h.setNewStatus(to);
        h.setChangedBy(by);
        return h;
    }

    // ---------- C-06: đồng ý báo giá ----------

    @Test
    @DisplayName("C-06: đồng ý đúng giá đang xem chuyển QUOTED sang ACCEPTED và ghi lịch sử")
    void accept_matchingPrice_movesToAccepted() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.QUOTED, new BigDecimal("2500000"));
        givenLocked(r);
        when(users.findById(CUSTOMER_ID)).thenReturn(Optional.of(user(CUSTOMER_ID, Role.CUSTOMER)));

        CustomRequestResponse res = service.accept(1L, CUSTOMER_ID, new BigDecimal("2500000"));

        assertThat(res.status()).isEqualTo(CustomStatus.ACCEPTED);
        CustomRequestHistory h = savedHistory();
        assertThat(h.getOldStatus()).isEqualTo(CustomStatus.QUOTED);
        assertThat(h.getNewStatus()).isEqualTo(CustomStatus.ACCEPTED);
    }

    @Test
    @DisplayName("TC-CUS-13: Staff vừa báo lại giá, khách đồng ý với giá cũ bị 409 QUOTE_CHANGED")
    void accept_stalePrice_throwsQuoteChanged() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.QUOTED, new BigDecimal("2800000"));
        givenLocked(r);

        assertThatThrownBy(() -> service.accept(1L, CUSTOMER_ID, new BigDecimal("2500000")))
            .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("QUOTE_CHANGED"));
        assertThat(r.getStatus()).isEqualTo(CustomStatus.QUOTED);
        verify(histories, never()).save(any());
    }

    @Test
    @DisplayName("C-06: so sánh giá theo giá trị, không theo scale của BigDecimal")
    void accept_priceComparedByValue() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.QUOTED, new BigDecimal("2500000"));
        givenLocked(r);
        when(users.findById(CUSTOMER_ID)).thenReturn(Optional.of(user(CUSTOMER_ID, Role.CUSTOMER)));

        assertThat(service.accept(1L, CUSTOMER_ID, new BigDecimal("2500000.00")).status())
            .isEqualTo(CustomStatus.ACCEPTED);
    }

    @Test
    @DisplayName("TC-CUS-11: đồng ý khi yêu cầu đang PENDING bị 409 INVALID_TRANSITION")
    void accept_whenPending_throwsInvalidTransition() {
        givenLocked(request(1L, CUSTOMER_ID, CustomStatus.PENDING, null));

        assertThatThrownBy(() -> service.accept(1L, CUSTOMER_ID, new BigDecimal("2500000")))
            .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("INVALID_TRANSITION"));
    }

    @Test
    @DisplayName("TC-CUS-10: đồng ý báo giá của yêu cầu người khác bị 403")
    void accept_otherOwner_throwsAccessDenied() {
        givenLocked(request(1L, OTHER_CUSTOMER_ID, CustomStatus.QUOTED, new BigDecimal("2500000")));

        assertThatThrownBy(() -> service.accept(1L, CUSTOMER_ID, new BigDecimal("2500000")))
            .isInstanceOf(AccessDeniedException.class);
        verify(histories, never()).save(any());
    }

    // ---------- C-06: hủy / từ chối báo giá ----------

    @Test
    @DisplayName("C-06: từ chối báo giá không ghi chú thì ghi sẵn \"Khách từ chối báo giá\"")
    void decline_quotedWithoutNote_usesDefaultNote() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.QUOTED, new BigDecimal("2500000"));
        givenLocked(r);
        when(users.findById(CUSTOMER_ID)).thenReturn(Optional.of(user(CUSTOMER_ID, Role.CUSTOMER)));

        CustomRequestResponse res = service.decline(1L, CUSTOMER_ID, null);

        assertThat(res.status()).isEqualTo(CustomStatus.CANCELLED);
        assertThat(savedHistory().getNote()).isEqualTo("Khách từ chối báo giá");
    }

    @Test
    @DisplayName("C-06: hủy yêu cầu PENDING giữ nguyên ghi chú của khách")
    void decline_pendingWithNote_keepsNote() {
        givenLocked(request(1L, CUSTOMER_ID, CustomStatus.PENDING, null));
        when(users.findById(CUSTOMER_ID)).thenReturn(Optional.of(user(CUSTOMER_ID, Role.CUSTOMER)));

        service.decline(1L, CUSTOMER_ID, "Đổi ý");

        assertThat(savedHistory().getNote()).isEqualTo("Đổi ý");
    }

    @Test
    @DisplayName("C-06: yêu cầu đã ACCEPTED thì khách không tự hủy được (409)")
    void decline_afterAccepted_throwsInvalidTransition() {
        givenLocked(request(1L, CUSTOMER_ID, CustomStatus.ACCEPTED, new BigDecimal("2500000")));

        assertThatThrownBy(() -> service.decline(1L, CUSTOMER_ID, null))
            .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("INVALID_TRANSITION"));
    }

    // ---------- A-07: báo giá ----------

    @Test
    @DisplayName("A-07: báo giá khi PENDING lưu giá và KHÔNG tự đổi trạng thái")
    void quote_pending_setsPriceKeepsStatus() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.PENDING, null);
        givenLocked(r);

        CustomRequestResponse res = service.quote(1L, new BigDecimal("2800000"));

        assertThat(res.quotedPrice()).isEqualByComparingTo("2800000");
        assertThat(res.status()).isEqualTo(CustomStatus.PENDING);
        verify(histories, never()).save(any());
    }

    @Test
    @DisplayName("A-07: báo lại giá khi đang QUOTED thay giá cũ")
    void quote_requote_replacesPrice() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.QUOTED, new BigDecimal("2500000"));
        givenLocked(r);

        assertThat(service.quote(1L, new BigDecimal("2800000")).quotedPrice()).isEqualByComparingTo("2800000");
    }

    @Test
    @DisplayName("A-07: không báo giá được khi yêu cầu đã ACCEPTED (409)")
    void quote_afterAccepted_throwsInvalidTransition() {
        givenLocked(request(1L, CUSTOMER_ID, CustomStatus.ACCEPTED, new BigDecimal("2500000")));

        assertThatThrownBy(() -> service.quote(1L, new BigDecimal("2800000")))
            .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("INVALID_TRANSITION"));
    }

    // ---------- A-07: chuyển trạng thái ----------

    @Test
    @DisplayName("A-07: chuyển PENDING sang QUOTED khi chưa có báo giá bị 409 QUOTE_REQUIRED")
    void changeStatus_quotedWithoutPrice_throwsQuoteRequired() {
        givenLocked(request(1L, CUSTOMER_ID, CustomStatus.PENDING, null));
        when(users.findById(STAFF_ID)).thenReturn(Optional.of(user(STAFF_ID, Role.STAFF)));

        assertThatThrownBy(() -> service.changeStatus(1L, CustomStatus.QUOTED, null, STAFF_ID))
            .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("QUOTE_REQUIRED"));
        verify(histories, never()).save(any());
    }

    @Test
    @DisplayName("A-07: chuyển PENDING sang QUOTED khi đã có báo giá thành công và ghi lịch sử")
    void changeStatus_quotedWithPrice_succeeds() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.PENDING, new BigDecimal("2800000"));
        givenLocked(r);
        User staff = user(STAFF_ID, Role.STAFF);
        when(users.findById(STAFF_ID)).thenReturn(Optional.of(staff));

        CustomRequestResponse res = service.changeStatus(1L, CustomStatus.QUOTED, " Đã tính linh kiện ", STAFF_ID);

        assertThat(res.status()).isEqualTo(CustomStatus.QUOTED);
        assertThat(res.customer()).isNotNull();              // phía Staff có thông tin khách
        CustomRequestHistory h = savedHistory();
        assertThat(h.getNote()).isEqualTo("Đã tính linh kiện");
        assertThat(h.getChangedBy()).isSameAs(staff);
    }

    @Test
    @DisplayName("A-07: nhảy cóc PENDING sang IN_PROGRESS bị 409 INVALID_TRANSITION")
    void changeStatus_skippingSteps_throwsInvalidTransition() {
        givenLocked(request(1L, CUSTOMER_ID, CustomStatus.PENDING, null));
        when(users.findById(STAFF_ID)).thenReturn(Optional.of(user(STAFF_ID, Role.STAFF)));

        assertThatThrownBy(() -> service.changeStatus(1L, CustomStatus.IN_PROGRESS, null, STAFF_ID))
            .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getCode()).isEqualTo("INVALID_TRANSITION"));
    }

    @Test
    @DisplayName("TC-CUS-12: Staff chuyển QUOTED sang ACCEPTED thay khách bị 403")
    void changeStatus_staffAcceptsForCustomer_throwsAccessDenied() {
        givenLocked(request(1L, CUSTOMER_ID, CustomStatus.QUOTED, new BigDecimal("2500000")));
        when(users.findById(STAFF_ID)).thenReturn(Optional.of(user(STAFF_ID, Role.STAFF)));

        assertThatThrownBy(() -> service.changeStatus(1L, CustomStatus.ACCEPTED, null, STAFF_ID))
            .isInstanceOf(AccessDeniedException.class);
        verify(histories, never()).save(any());
    }

    @Test
    @DisplayName("A-07: Customer gọi changeStatus bị 403 (lớp phòng thủ ở Service, TC-CUS-08 kiểm ở URL)")
    void changeStatus_customerActor_throwsAccessDenied() {
        givenLocked(request(1L, CUSTOMER_ID, CustomStatus.PENDING, null));
        when(users.findById(CUSTOMER_ID)).thenReturn(Optional.of(user(CUSTOMER_ID, Role.CUSTOMER)));

        assertThatThrownBy(() -> service.changeStatus(1L, CustomStatus.CANCELLED, null, CUSTOMER_ID))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("A-07: Admin đi hết luồng ACCEPTED, IN_PROGRESS, COMPLETED, mỗi bước một dòng lịch sử")
    void changeStatus_adminFullFlow() {
        CustomRequest r = request(1L, CUSTOMER_ID, CustomStatus.ACCEPTED, new BigDecimal("2500000"));
        givenLocked(r);
        when(users.findById(ADMIN_ID)).thenReturn(Optional.of(user(ADMIN_ID, Role.ADMIN)));

        service.changeStatus(1L, CustomStatus.IN_PROGRESS, null, ADMIN_ID);
        service.changeStatus(1L, CustomStatus.COMPLETED, "Đã bàn giao", ADMIN_ID);

        assertThat(r.getStatus()).isEqualTo(CustomStatus.COMPLETED);
        verify(histories, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    @DisplayName("A-07: yêu cầu không tồn tại trả 404")
    void changeStatus_missing_throwsNotFound() {
        when(requests.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(99L, CustomStatus.QUOTED, null, STAFF_ID))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
