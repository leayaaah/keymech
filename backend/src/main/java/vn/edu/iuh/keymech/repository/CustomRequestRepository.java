package vn.edu.iuh.keymech.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.CustomRequest;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

public interface CustomRequestRepository extends JpaRepository<CustomRequest, Long> {
    Page<CustomRequest> findByUserId(Long userId, Pageable pageable);
    Optional<CustomRequest> findByIdAndUserId(Long id, Long userId);
    Optional<CustomRequest> findByRequestCode(String requestCode);
    Page<CustomRequest> findByStatus(CustomStatus status, Pageable pageable);

    // Quy ước: không xóa vật lý custom_requests
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(CustomRequest entity) { throw new UnsupportedOperationException(); }
}
