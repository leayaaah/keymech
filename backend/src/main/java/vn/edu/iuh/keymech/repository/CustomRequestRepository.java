package vn.edu.iuh.keymech.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import vn.edu.iuh.keymech.model.CustomRequest;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

public interface CustomRequestRepository extends JpaRepository<CustomRequest, Long> {
    Page<CustomRequest> findByUserId(Long userId, Pageable pageable);
    Optional<CustomRequest> findByIdAndUserId(Long id, Long userId);
    Optional<CustomRequest> findByRequestCode(String requestCode);
    Page<CustomRequest> findByStatus(CustomStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from CustomRequest r where r.id = :id")
    Optional<CustomRequest> lockById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from CustomRequest r where r.id = :id and r.user.id = :userId")
    Optional<CustomRequest> lockByIdAndUserId(long id, long userId);

    // Quy ước: không xóa vật lý custom_requests
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(CustomRequest entity) { throw new UnsupportedOperationException(); }
}
