package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import vn.edu.iuh.keymech.model.CustomRequest;
import vn.edu.iuh.keymech.model.CustomRequestComponent;
import vn.edu.iuh.keymech.model.CustomRequestHistory;
import vn.edu.iuh.keymech.model.enums.CustomStatus;
import vn.edu.iuh.keymech.model.enums.Role;

public record CustomRequestResponse(
        Long id, String requestCode, Long customerId,
        String layout, String switchType, String switchName, String caseMaterial, String keycapSet,
        String ledType, boolean lubeSwitch, boolean hotSwap, BigDecimal budget, String note,
        CustomStatus status, BigDecimal quotedPrice, String quoteNote, LocalDateTime createdAt,
        List<History> history, List<Component> components) {

    public static final String SWITCH_SEPARATOR = " • ";
    private static final Set<CustomStatus> PUBLISHED_STATUSES = Set.of(
            CustomStatus.QUOTED, CustomStatus.ACCEPTED, CustomStatus.IN_PROGRESS, CustomStatus.COMPLETED);

    public static CustomRequestResponse from(CustomRequest request, List<CustomRequestHistory> history,
                                             boolean staffView, boolean includeComponents) {
        boolean showQuote = staffView || PUBLISHED_STATUSES.contains(request.getStatus());
        String[] switchParts = request.getSwitchType().split(SWITCH_SEPARATOR, 2);
        String quoteNote = history.stream()
                .filter(entry -> entry.getOldStatus() == entry.getNewStatus())
                .findFirst().map(CustomRequestHistory::getNote).orElse(null);
        return new CustomRequestResponse(request.getId(), request.getRequestCode(),
                staffView ? request.getUser().getId() : null, request.getLayout(), switchParts[0],
                switchParts.length == 2 ? switchParts[1] : null, request.getCaseMaterial(), request.getKeycapSet(),
                request.getRgbOption(), request.isLubing(), request.isHotSwap(), request.getBudget(),
                request.getNote(), request.getStatus(), showQuote ? request.getQuotedPrice() : null,
                showQuote ? quoteNote : null, request.getCreatedAt(),
                history.stream().filter(entry -> entry.getOldStatus() != entry.getNewStatus()).map(History::from).toList(),
                includeComponents ? request.getComponents().stream()
                        .sorted(Comparator.comparing(CustomRequestComponent::slot)).map(Component::from).toList() : List.of());
    }

    public record Component(CustomRequestComponent.Slot slot, Long productId, String name, int quantity, BigDecimal unitPrice) {
        private static Component from(CustomRequestComponent part) {
            return new Component(part.slot(), part.productId(), part.name(), part.quantity(), part.unitPrice());
        }
    }

    public record History(Long id, CustomStatus fromStatus, CustomStatus toStatus, Role actorRole,
                          String note, LocalDateTime createdAt) {
        static History from(CustomRequestHistory entry) {
            return new History(entry.getId(), entry.getOldStatus(), entry.getNewStatus(),
                    entry.getChangedBy().getRole(), entry.getNote(), entry.getChangedAt());
        }
    }
}
