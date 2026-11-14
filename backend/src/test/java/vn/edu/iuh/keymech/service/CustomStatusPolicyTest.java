package vn.edu.iuh.keymech.service;

import static org.assertj.core.api.Assertions.assertThat;
import static vn.edu.iuh.keymech.model.enums.CustomStatus.*;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

class CustomStatusPolicyTest {

    @Test
    @DisplayName("A-07: chỉ các bước trong sơ đồ mới hợp lệ, bước nhảy cóc bị từ chối")
    void isAllowed_followsStateDiagram() {
        assertThat(CustomStatusPolicy.isAllowed(PENDING, QUOTED)).isTrue();
        assertThat(CustomStatusPolicy.isAllowed(PENDING, REJECTED)).isTrue();
        assertThat(CustomStatusPolicy.isAllowed(QUOTED, ACCEPTED)).isTrue();
        assertThat(CustomStatusPolicy.isAllowed(ACCEPTED, IN_PROGRESS)).isTrue();
        assertThat(CustomStatusPolicy.isAllowed(IN_PROGRESS, COMPLETED)).isTrue();

        assertThat(CustomStatusPolicy.isAllowed(PENDING, IN_PROGRESS)).isFalse();
        assertThat(CustomStatusPolicy.isAllowed(PENDING, ACCEPTED)).isFalse();
        assertThat(CustomStatusPolicy.isAllowed(QUOTED, IN_PROGRESS)).isFalse();
        assertThat(CustomStatusPolicy.isAllowed(ACCEPTED, COMPLETED)).isFalse();
    }

    @Test
    @DisplayName("A-07: trạng thái cuối không chuyển đi đâu được")
    void terminalStatuses_haveNoNext() {
        for (CustomStatus terminal : new CustomStatus[] {COMPLETED, REJECTED, CANCELLED}) {
            assertThat(CustomStatusPolicy.nextForStaff(terminal)).isEmpty();
            assertThat(CustomStatusPolicy.nextForCustomer(terminal)).isEmpty();
        }
    }

    @Test
    @DisplayName("TC-CUS-12: Staff không được làm bước khách đồng ý báo giá")
    void staff_cannotAcceptOnBehalfOfCustomer() {
        assertThat(CustomStatusPolicy.isStaffStep(QUOTED, ACCEPTED)).isFalse();
        assertThat(CustomStatusPolicy.nextForStaff(QUOTED)).containsExactly(CANCELLED);
    }

    @Test
    @DisplayName("C-06: khách chỉ làm đồng ý, hủy và từ chối báo giá")
    void customer_onlyOwnSteps() {
        assertThat(CustomStatusPolicy.nextForCustomer(PENDING)).containsExactly(CANCELLED);
        assertThat(CustomStatusPolicy.nextForCustomer(QUOTED)).containsExactlyInAnyOrder(ACCEPTED, CANCELLED);
        assertThat(CustomStatusPolicy.nextForCustomer(ACCEPTED)).isEmpty();
        assertThat(CustomStatusPolicy.nextForCustomer(IN_PROGRESS)).isEmpty();
    }

    @Test
    @DisplayName("A-07: Staff làm được mọi bước hợp lệ còn lại")
    void staff_doesRestOfDiagram() {
        assertThat(CustomStatusPolicy.nextForStaff(PENDING)).containsExactlyInAnyOrder(QUOTED, REJECTED, CANCELLED);
        assertThat(CustomStatusPolicy.nextForStaff(ACCEPTED)).containsExactlyInAnyOrder(IN_PROGRESS, CANCELLED);
        assertThat(CustomStatusPolicy.nextForStaff(IN_PROGRESS)).containsExactlyInAnyOrder(COMPLETED, CANCELLED);
    }

    @Test
    @DisplayName("Mọi trạng thái đều có nhãn tiếng Việt")
    void label_existsForEveryStatus() {
        for (CustomStatus s : CustomStatus.values()) {
            assertThat(CustomStatusPolicy.label(s)).isNotBlank();
        }
        assertThat(Set.of(CustomStatusPolicy.label(QUOTED))).containsExactly("Đã báo giá");
    }
}
