package vn.edu.iuh.keymech.service;

import static vn.edu.iuh.keymech.model.enums.CustomStatus.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

/**
 * Bảng chuyển trạng thái yêu cầu Custom (mục 9 của kế hoạch). Lớp thuần Java, không phụ thuộc Spring,
 * không dùng Check hay Trigger của CSDL: toàn bộ luật nằm ở đây.
 */
public final class CustomStatusPolicy {

    private static final Map<CustomStatus, Set<CustomStatus>> ALLOWED = new EnumMap<>(CustomStatus.class);

    static {
        ALLOWED.put(PENDING, Set.of(QUOTED, REJECTED, CANCELLED));
        ALLOWED.put(QUOTED, Set.of(ACCEPTED, CANCELLED));
        ALLOWED.put(ACCEPTED, Set.of(IN_PROGRESS, CANCELLED));
        ALLOWED.put(IN_PROGRESS, Set.of(COMPLETED, CANCELLED));
        ALLOWED.put(COMPLETED, Set.of());
        ALLOWED.put(REJECTED, Set.of());
        ALLOWED.put(CANCELLED, Set.of());
    }

    private CustomStatusPolicy() {}

    /** Bước có nằm trong sơ đồ chuyển trạng thái hay không (chưa xét ai thực hiện). */
    public static boolean isAllowed(CustomStatus from, CustomStatus to) {
        return ALLOWED.get(from).contains(to);
    }

    /** Các bước khách được tự làm trên yêu cầu của chính mình: đồng ý, hủy, từ chối báo giá. */
    public static boolean isCustomerStep(CustomStatus from, CustomStatus to) {
        return (from == QUOTED && to == ACCEPTED)
            || (from == PENDING && to == CANCELLED)
            || (from == QUOTED && to == CANCELLED);
    }

    /** Staff và Admin làm mọi bước hợp lệ, trừ bước khách đồng ý báo giá. */
    public static boolean isStaffStep(CustomStatus from, CustomStatus to) {
        return isAllowed(from, to) && !(from == QUOTED && to == ACCEPTED);
    }

    public static Set<CustomStatus> nextForCustomer(CustomStatus from) {
        return filter(from, true);
    }

    public static Set<CustomStatus> nextForStaff(CustomStatus from) {
        return filter(from, false);
    }

    private static Set<CustomStatus> filter(CustomStatus from, boolean customer) {
        Set<CustomStatus> result = new java.util.LinkedHashSet<>();
        for (CustomStatus to : CustomStatus.values()) {
            if (customer ? isCustomerStep(from, to) : isStaffStep(from, to)) {
                result.add(to);
            }
        }
        return result;
    }

    /** Tên tiếng Việt dùng trong thông báo lỗi (cùng nhãn với giao diện). */
    public static String label(CustomStatus s) {
        return switch (s) {
            case PENDING -> "Chờ tiếp nhận";
            case QUOTED -> "Đã báo giá";
            case ACCEPTED -> "Khách đã đồng ý";
            case IN_PROGRESS -> "Đang lắp ráp";
            case COMPLETED -> "Hoàn thành";
            case REJECTED -> "Từ chối";
            case CANCELLED -> "Đã hủy";
        };
    }
}
