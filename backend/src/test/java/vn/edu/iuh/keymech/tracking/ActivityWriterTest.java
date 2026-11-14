package vn.edu.iuh.keymech.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.iuh.keymech.model.UserActivity;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.repository.UserActivityRepository;

@ExtendWith(MockitoExtension.class)
class ActivityWriterTest {

    private static final String VISITOR = "11111111-1111-1111-1111-111111111111";

    @Mock UserActivityRepository repository;
    @InjectMocks ActivityWriter writer;

    private static ActivityEvent event(EventType type, Long userId, Long productId, Long categoryId, String keyword) {
        return new ActivityEvent(VISITOR, userId, type, productId, categoryId, keyword);
    }

    @Test
    @DisplayName("TC-TRK-01: ghi dòng với đủ visitor_id, loại sự kiện và sản phẩm")
    void save_persistsActivity() {
        when(repository.existsByVisitorIdAndEventTypeAndProductIdAndCreatedAtAfter(
            anyString(), any(), anyLong(), any(LocalDateTime.class))).thenReturn(false);
        when(repository.findCategoryIdByProductId(5L)).thenReturn(2L);

        writer.save(event(EventType.VIEW_PRODUCT, null, 5L, null, null));

        ArgumentCaptor<UserActivity> c = ArgumentCaptor.forClass(UserActivity.class);
        verify(repository).save(c.capture());
        UserActivity a = c.getValue();
        assertThat(a.getVisitorId()).isEqualTo(VISITOR);
        assertThat(a.getUserId()).isNull();
        assertThat(a.getEventType()).isEqualTo(EventType.VIEW_PRODUCT);
        assertThat(a.getProductId()).isEqualTo(5L);
        assertThat(a.getCategoryId()).isEqualTo(2L);       // tự điền từ sản phẩm
    }

    @Test
    @DisplayName("VIEW_PRODUCT xem lại trong 60 giây của cùng phiên thì không ghi thêm")
    void save_duplicateView_skipped() {
        when(repository.existsByVisitorIdAndEventTypeAndProductIdAndCreatedAtAfter(
            anyString(), any(), anyLong(), any(LocalDateTime.class))).thenReturn(true);

        writer.save(event(EventType.VIEW_PRODUCT, null, 5L, null, null));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Chỉ VIEW_PRODUCT bị lọc trùng; ADD_TO_CART và SEARCH luôn được ghi")
    void save_otherEvents_notDeduplicated() {
        writer.save(event(EventType.SEARCH, null, null, 1L, "keychron"));

        verify(repository, never()).existsByVisitorIdAndEventTypeAndProductIdAndCreatedAtAfter(
            anyString(), any(), anyLong(), any(LocalDateTime.class));
        ArgumentCaptor<UserActivity> c = ArgumentCaptor.forClass(UserActivity.class);
        verify(repository).save(c.capture());
        assertThat(c.getValue().getKeyword()).isEqualTo("keychron");
        assertThat(c.getValue().getCategoryId()).isEqualTo(1L);   // giữ category_id người gọi truyền
    }

    @Test
    @DisplayName("TC-TRK-05: lỗi CSDL khi ghi chỉ được nuốt và ghi log, không ném ra ngoài")
    void save_repositoryFails_doesNotThrow() {
        when(repository.save(any())).thenThrow(new RuntimeException("DB down"));

        assertThatCode(() -> writer.save(event(EventType.ADD_TO_CART, 7L, null, null, null)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("TC-TRK-02: claim gắn user_id cho các dòng chưa có chủ của visitor_id")
    void claim_updatesRows() {
        when(repository.claimVisitor(VISITOR, 7L)).thenReturn(4);

        assertThat(writer.claim(VISITOR, 7L)).isEqualTo(4);
    }
}
