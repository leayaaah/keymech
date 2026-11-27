package vn.edu.iuh.keymech.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.dto.CustomRequestInput;
import vn.edu.iuh.keymech.dto.CustomRequestResponse;
import vn.edu.iuh.keymech.dto.PageResponse;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.exception.ResourceNotFoundException;
import vn.edu.iuh.keymech.model.CustomRequest;
import vn.edu.iuh.keymech.model.CustomRequestHistory;
import vn.edu.iuh.keymech.model.enums.CustomStatus;
import vn.edu.iuh.keymech.repository.CustomRequestHistoryRepository;
import vn.edu.iuh.keymech.repository.CustomRequestRepository;
import vn.edu.iuh.keymech.repository.UserRepository;

import static vn.edu.iuh.keymech.model.enums.CustomStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomRequestService {
    private static final Map<CustomStatus, Set<CustomStatus>> TRANSITIONS = Map.of(
            PENDING, Set.of(QUOTED, REJECTED, CANCELLED),
            QUOTED, Set.of(ACCEPTED, CANCELLED),
            ACCEPTED, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(COMPLETED, CANCELLED));

    private final CustomRequestRepository requests;
    private final CustomRequestHistoryRepository history;
    private final UserRepository users;

    @Transactional
    public CustomRequestResponse create(long actorId, CustomRequestInput input) {
        CustomRequest request = new CustomRequest();
        String code = UUID.randomUUID().toString().replace("-", "");
        request.setRequestCode("CUS-" + code.substring(0, 26));
        request.setUser(users.getReferenceById(actorId));
        request.setLayout(input.layout());
        request.setSwitchType(input.switchType() + CustomRequestResponse.SWITCH_SEPARATOR + input.switchName().strip());
        request.setCaseMaterial(input.caseMaterial());
        request.setKeycapSet(input.keycapSet());
        request.setRgbOption(input.ledType());
        request.setLubing(Boolean.TRUE.equals(input.lubeSwitch()));
        request.setHotSwap(!Boolean.FALSE.equals(input.hotSwap()));
        request.setBudget(input.budget());
        request.setNote(input.note());
        requests.save(request);
        record(request, null, "Đã gửi yêu cầu Custom.", actorId);
        return detail(request, false);
    }

    public PageResponse<CustomRequestResponse> listMine(long userId, int page, int size) {
        return summarize(requests.findByUserId(userId, pagination(page, size)), false);
    }

    public PageResponse<CustomRequestResponse> listForStaff(CustomStatus status, int page, int size) {
        var pageable = pagination(page, size);
        return summarize(status == null ? requests.findAll(pageable) : requests.findByStatus(status, pageable), true);
    }

    public CustomRequestResponse getMine(long userId, long id) {
        return detail(requests.findByIdAndUserId(id, userId).orElseThrow(this::notFound), false);
    }

    public CustomRequestResponse getForStaff(long id) {
        return detail(requests.findById(id).orElseThrow(this::notFound), true);
    }

    @Transactional
    public CustomRequestResponse accept(long userId, long id, BigDecimal quotedPrice) {
        CustomRequest request = lockMine(id, userId);
        if (request.getStatus() != QUOTED) throw invalidTransition();
        if (request.getQuotedPrice() == null || request.getQuotedPrice().compareTo(quotedPrice) != 0) {
            throw new BusinessException("QUOTE_CHANGED", "Báo giá đã thay đổi. Vui lòng tải lại.");
        }
        transition(request, ACCEPTED, "Khách đồng ý báo giá.", userId);
        return detail(request, false);
    }

    @Transactional
    public CustomRequestResponse decline(long userId, long id, String note) {
        CustomRequest request = lockMine(id, userId);
        CustomStatus status = request.getStatus();
        if (status != PENDING && status != QUOTED) throw invalidTransition();
        if (note == null || note.isBlank()) {
            note = status == QUOTED ? "Khách từ chối báo giá." : "Khách hủy yêu cầu.";
        }
        transition(request, CANCELLED, note, userId);
        return detail(request, false);
    }

    @Transactional
    public CustomRequestResponse quote(long id, long actorId, CustomRequestInput.Quote input) {
        CustomRequest request = requests.lockById(id).orElseThrow(this::notFound);
        CustomStatus status = request.getStatus();
        if (status != PENDING && status != QUOTED) throw invalidTransition();
        request.setQuotedPrice(input.quotedPrice());
        // Ghi lần lưu báo giá vào lịch sử hiện có, giữ nguyên trạng thái.
        record(request, status, input.quoteNote(), actorId);
        return detail(request, true);
    }

    @Transactional
    public CustomRequestResponse changeStatus(long id, long actorId, CustomRequestInput.Transition input) {
        if (input.status() == ACCEPTED) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Chỉ khách hàng được đồng ý báo giá.");
        }
        CustomRequest request = requests.lockById(id).orElseThrow(this::notFound);
        transition(request, input.status(), input.note(), actorId);
        return detail(request, true);
    }

    private void transition(CustomRequest request, CustomStatus next, String note, long actorId) {
        CustomStatus current = request.getStatus();
        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(next)) throw invalidTransition();
        if (next == QUOTED && request.getQuotedPrice() == null) {
            throw new BusinessException("QUOTE_REQUIRED", "Vui lòng nhập báo giá trước khi gửi cho khách hàng.");
        }
        if (next == REJECTED && (note == null || note.isBlank())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Vui lòng nhập lý do từ chối.");
        }
        request.setStatus(next);
        record(request, current, note, actorId);
    }

    private void record(CustomRequest request, CustomStatus previous, String note, long actorId) {
        CustomRequestHistory entry = new CustomRequestHistory();
        entry.setCustomRequest(request);
        entry.setOldStatus(previous);
        entry.setNewStatus(request.getStatus());
        entry.setNote(note);
        entry.setChangedBy(users.getReferenceById(actorId));
        history.save(entry);
    }

    private CustomRequest lockMine(long id, long userId) {
        return requests.lockByIdAndUserId(id, userId).orElseThrow(this::notFound);
    }

    private CustomRequestResponse detail(CustomRequest request, boolean staffView) {
        return CustomRequestResponse.from(request,
                history.findByCustomRequestIdOrderByChangedAtDescIdDesc(request.getId()), staffView);
    }

    private PageResponse<CustomRequestResponse> summarize(Page<CustomRequest> page, boolean staffView) {
        return PageResponse.of(page.map(request -> CustomRequestResponse.from(request, List.of(), staffView)));
    }

    private PageRequest pagination(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 100),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    private BusinessException invalidTransition() {
        return new BusinessException("INVALID_TRANSITION", "Không thể thực hiện thao tác ở trạng thái hiện tại.");
    }

    private ResourceNotFoundException notFound() {
        return new ResourceNotFoundException("Không tìm thấy yêu cầu Custom.");
    }
}
