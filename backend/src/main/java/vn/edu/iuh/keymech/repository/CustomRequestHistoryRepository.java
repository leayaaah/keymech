package vn.edu.iuh.keymech.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.CustomRequestHistory;

public interface CustomRequestHistoryRepository extends JpaRepository<CustomRequestHistory, Long> {
    List<CustomRequestHistory> findByCustomRequestIdOrderByChangedAtAsc(Long customRequestId);
}
