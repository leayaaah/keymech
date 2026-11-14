package vn.edu.iuh.keymech.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.keymech.model.CustomRequestHistory;

public interface CustomRequestHistoryRepository extends JpaRepository<CustomRequestHistory, Long> {
    List<CustomRequestHistory> findByCustomRequestIdOrderByChangedAtAsc(Long customRequestId);

    /**
     * Timeline của một yêu cầu, nạp sẵn người thực hiện. DATETIME chỉ chính xác đến giây
     * nên sắp xếp thêm theo id để giữ đúng thứ tự khi hai bước cùng giây.
     */
    @Query("SELECT h FROM CustomRequestHistory h JOIN FETCH h.changedBy "
         + "WHERE h.customRequest.id = :requestId ORDER BY h.changedAt ASC, h.id ASC")
    List<CustomRequestHistory> findTimeline(@Param("requestId") Long requestId);
}
