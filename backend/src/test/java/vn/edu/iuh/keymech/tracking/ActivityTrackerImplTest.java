package vn.edu.iuh.keymech.tracking;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.iuh.keymech.model.enums.EventType;

@ExtendWith(MockitoExtension.class)
class ActivityTrackerImplTest {
    @Mock private ActivityWriter writer;

    @Test
    @DisplayName("TC-TRK-02: login attaches guest events only after pending async writes finish")
    void attachGuestActivities_waitsForPendingWrite() {
        CompletableFuture<Void> pendingWrite = new CompletableFuture<>();
        when(writer.write(EventType.VIEW_PRODUCT, "visitor-1", null, 21L, null, null))
            .thenReturn(pendingWrite);
        when(writer.attachGuestActivities("visitor-1", 8L))
            .thenReturn(CompletableFuture.completedFuture(null));
        ActivityTracker tracker = new ActivityTrackerImpl(writer);

        tracker.record(EventType.VIEW_PRODUCT, "visitor-1", null, 21L, null, null);
        tracker.attachGuestActivities("visitor-1", 8L);

        verify(writer, never()).attachGuestActivities("visitor-1", 8L);
        pendingWrite.complete(null);
        verify(writer).attachGuestActivities("visitor-1", 8L);
    }
}
