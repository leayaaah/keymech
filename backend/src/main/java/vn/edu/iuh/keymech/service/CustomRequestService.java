package vn.edu.iuh.keymech.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.*;
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
import vn.edu.iuh.keymech.util.CodeGenerator;

/**
 * Nghiệp vụ yêu cầu Custom: C-03, C-04 (Custom), C-06 phía Customer và A-07 phía Staff/Admin.
 * Mỗi lần đổi trạng thái ghi một dòng lịch sử trong cùng transaction.
 */
@Slf4j
@Service
public class CustomRequestService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final String CUSTOMER_DECLINED_NOTE = "Khách từ chối báo giá";

    private final CustomRequestRepository requests;
    private final CustomRequestHistoryRepository histories;
    private final UserRepository users;

    public CustomRequestService(CustomRequestRepository requests,
                                CustomRequestHistoryRepository histories,
                                UserRepository users) {
        this.requests = requests;
        this.histories = histories;
        this.users = users;
    }

    // ===================== Phía Customer =====================

    /** C-03: tạo yêu cầu PENDING và ghi dòng lịch sử đầu tiên (old_status để trống). */
    @Transactional
    public CustomRequestResponse create(Long currentUserId, CreateCustomRequest form) {
        User customer = loadUser(currentUserId);

        CustomRequest r = new CustomRequest();
        r.setRequestCode(CodeGenerator.customCode());
        r.setUser(customer);
        r.setLayout(form.layout());
        r.setSwitchType(form.switchType().trim());
        r.setCaseMaterial(blankToNull(form.caseMaterial()));
        r.setKeycapSet(blankToNull(form.keycapSet()));
        r.setLubing(Boolean.TRUE.equals(form.lubing()));
        r.setHotSwap(form.hotSwap() == null || form.hotSwap());   // mặc định có hot-swap
        r.setRgbOption(blankToNull(form.rgbOption()));
        r.setBudget(form.budget());
        r.setNote(blankToNull(form.note()));
        r.setStatus(CustomStatus.PENDING);
        requests.save(r);

        saveHistory(r, null, CustomStatus.PENDING, null, customer);
        log.info("Tạo yêu cầu Custom {}", r.getRequestCode());   // không ghi dữ liệu cá nhân
        return toResponse(r, false);
    }

    /** C-04: danh sách yêu cầu của chính mình, mới nhất trước. */
    @Transactional(readOnly = true)
    public PageResponse<CustomRequestResponse> listMy(Long currentUserId, int page, int size) {
        Page<CustomRequest> result = requests.findByUserId(currentUserId, pageable(page, size));
        return PageResponse.of(result, r -> toResponse(r, false));
    }

    /** C-04: chi tiết và timeline; yêu cầu của người khác trả 403. */
    @Transactional(readOnly = true)
    public CustomRequestDetailResponse getMy(Long id, Long currentUserId) {
        CustomRequest r = requests.findById(id).orElseThrow(ResourceNotFoundException::new);
        assertOwner(r, currentUserId);
        return detail(r, false);
    }

    /** C-06: đồng ý báo giá (QUOTED -> ACCEPTED), kèm giá đang xem để chống đồng ý nhầm giá cũ. */
    @Transactional
    public CustomRequestResponse accept(Long id, Long currentUserId, BigDecimal viewedPrice) {
        CustomRequest r = lockForUpdate(id);
        assertOwner(r, currentUserId);
        if (r.getStatus() != CustomStatus.QUOTED) {
            throw invalidTransition(r.getStatus(), CustomStatus.ACCEPTED);
        }
        if (r.getQuotedPrice() == null || r.getQuotedPrice().compareTo(viewedPrice) != 0) {
            throw new BusinessException("QUOTE_CHANGED",
                "Báo giá đã thay đổi, vui lòng xem lại giá mới trước khi đồng ý");
        }
        transition(r, CustomStatus.ACCEPTED, null, loadUser(currentUserId), false);
        return toResponse(r, false);
    }

    /** C-06: hủy yêu cầu (PENDING) hoặc từ chối báo giá (QUOTED), cùng chuyển sang CANCELLED. */
    @Transactional
    public CustomRequestResponse decline(Long id, Long currentUserId, String note) {
        CustomRequest r = lockForUpdate(id);
        assertOwner(r, currentUserId);
        CustomStatus from = r.getStatus();
        if (from != CustomStatus.PENDING && from != CustomStatus.QUOTED) {
            throw invalidTransition(from, CustomStatus.CANCELLED);
        }
        String finalNote = blankToNull(note);
        if (finalNote == null && from == CustomStatus.QUOTED) {
            finalNote = CUSTOMER_DECLINED_NOTE;
        }
        transition(r, CustomStatus.CANCELLED, finalNote, loadUser(currentUserId), false);
        return toResponse(r, false);
    }

    // ===================== Phía Staff / Admin =====================

    /** A-07: danh sách mọi yêu cầu, lọc theo trạng thái (status null là tất cả). */
    @Transactional(readOnly = true)
    public PageResponse<CustomRequestResponse> listForStaff(CustomStatus status, int page, int size) {
        Pageable p = pageable(page, size);
        Page<CustomRequest> result = status == null
            ? requests.findAllBy(p)
            : requests.findWithUserByStatus(status, p);
        return PageResponse.of(result, r -> toResponse(r, true));
    }

    @Transactional(readOnly = true)
    public CustomRequestDetailResponse getForStaff(Long id) {
        CustomRequest r = requests.findById(id).orElseThrow(ResourceNotFoundException::new);
        return detail(r, true);
    }

    /**
     * A-07: nhập hoặc thay báo giá. Chỉ khi PENDING hoặc QUOTED và không tự đổi trạng thái;
     * muốn sang QUOTED phải gọi tiếp changeStatus. Báo lại giá khi đang QUOTED thì khách phải
     * đồng ý đúng giá mới (kiểm ở accept).
     */
    @Transactional
    public CustomRequestResponse quote(Long id, BigDecimal price) {
        CustomRequest r = lockForUpdate(id);
        if (r.getStatus() != CustomStatus.PENDING && r.getStatus() != CustomStatus.QUOTED) {
            throw new BusinessException("INVALID_TRANSITION",
                "Chỉ báo giá được khi yêu cầu đang ở trạng thái Chờ tiếp nhận hoặc Đã báo giá");
        }
        r.setQuotedPrice(price);
        return toResponse(r, true);
    }

    /** A-07: chuyển trạng thái đúng luồng (trừ bước khách đồng ý) và ghi lịch sử. */
    @Transactional
    public CustomRequestResponse changeStatus(Long id, CustomStatus next, String note, Long actorId) {
        CustomRequest r = lockForUpdate(id);
        User actor = loadUser(actorId);
        if (actor.getRole() == Role.CUSTOMER) {
            throw new AccessDeniedException("Không có quyền với thao tác này");
        }
        transition(r, next, blankToNull(note), actor, true);
        return toResponse(r, true);
    }

    // ===================== Lõi chuyển trạng thái =====================

    /**
     * Kiểm tra bước hợp lệ, kiểm tra người thực hiện, rồi đổi trạng thái và ghi lịch sử.
     * Thứ tự kiểm: sai sơ đồ (409 INVALID_TRANSITION) trước, sai người (403) sau, thiếu giá (409) cuối.
     */
    private void transition(CustomRequest r, CustomStatus next, String note, User actor, boolean staffActor) {
        CustomStatus from = r.getStatus();
        if (!CustomStatusPolicy.isAllowed(from, next)) {
            throw invalidTransition(from, next);
        }
        boolean permitted = staffActor
            ? CustomStatusPolicy.isStaffStep(from, next)
            : CustomStatusPolicy.isCustomerStep(from, next);
        if (!permitted) {
            throw new AccessDeniedException("Không có quyền thực hiện bước này");
        }
        if (next == CustomStatus.QUOTED && r.getQuotedPrice() == null) {
            throw new BusinessException("QUOTE_REQUIRED",
                "Cần nhập báo giá trước khi chuyển sang Đã báo giá");
        }
        saveHistory(r, from, next, note, actor);
        r.setStatus(next);   // updated_at do @PreUpdate của entity đặt
    }

    private void saveHistory(CustomRequest r, CustomStatus oldStatus, CustomStatus newStatus,
                             String note, User actor) {
        CustomRequestHistory h = new CustomRequestHistory();
        h.setCustomRequest(r);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setNote(note);
        h.setChangedBy(actor);
        histories.save(h);
    }

    // ===================== Hỗ trợ =====================

    private CustomRequest lockForUpdate(Long id) {
        return requests.findByIdForUpdate(id).orElseThrow(ResourceNotFoundException::new);
    }

    private User loadUser(Long id) {
        return users.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    private void assertOwner(CustomRequest r, Long currentUserId) {
        if (!r.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Không có quyền với yêu cầu này");
        }
    }

    private BusinessException invalidTransition(CustomStatus from, CustomStatus to) {
        return new BusinessException("INVALID_TRANSITION",
            "Không thể chuyển từ \"" + CustomStatusPolicy.label(from) + "\" sang \""
                + CustomStatusPolicy.label(to) + "\"");
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private CustomRequestDetailResponse detail(CustomRequest r, boolean staffView) {
        List<CustomHistoryResponse> timeline = histories.findTimeline(r.getId()).stream()
            .map(h -> toHistory(h, staffView))
            .toList();
        List<CustomStatus> next = staffView
            ? List.copyOf(CustomStatusPolicy.nextForStaff(r.getStatus()))
            : List.copyOf(CustomStatusPolicy.nextForCustomer(r.getStatus()));
        return new CustomRequestDetailResponse(toResponse(r, staffView), timeline, next);
    }

    private CustomRequestResponse toResponse(CustomRequest r, boolean withCustomer) {
        CustomerSummary customer = null;
        if (withCustomer) {
            User u = r.getUser();
            customer = new CustomerSummary(u.getId(), u.getFullName(), u.getEmail(), u.getPhone());
        }
        return new CustomRequestResponse(
            r.getId(), r.getRequestCode(), r.getLayout(), r.getCaseMaterial(), r.getSwitchType(),
            r.getKeycapSet(), r.isLubing(), r.isHotSwap(), r.getRgbOption(), r.getBudget(),
            r.getNote(), r.getQuotedPrice(), r.getStatus(), r.getCreatedAt(), r.getUpdatedAt(), customer);
    }

    private CustomHistoryResponse toHistory(CustomRequestHistory h, boolean staffView) {
        User by = h.getChangedBy();
        String actor = staffView
            ? by.getFullName() + " (" + by.getRole().name() + ")"
            : (by.getRole() == Role.CUSTOMER ? "Bạn" : "Cửa hàng");
        return new CustomHistoryResponse(
            h.getId(), h.getOldStatus(), h.getNewStatus(), h.getNote(), actor, h.getChangedAt());
    }
}
