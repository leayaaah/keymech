package vn.edu.iuh.keymech.repository;

<<<<<<< HEAD
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
>>>>>>> origin/main
import vn.edu.iuh.keymech.model.CustomRequest;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

public interface CustomRequestRepository extends JpaRepository<CustomRequest, Long> {
    Page<CustomRequest> findByUserId(Long userId, Pageable pageable);
    Optional<CustomRequest> findByIdAndUserId(Long id, Long userId);
    Optional<CustomRequest> findByRequestCode(String requestCode);
    Page<CustomRequest> findByStatus(CustomStatus status, Pageable pageable);

<<<<<<< HEAD
    /**
     * Khóa dòng khi đổi trạng thái hoặc báo giá, để hai request đồng thời
     * (ví dụ khách đồng ý trong lúc Staff báo lại giá) không ghi đè nhau.
     * Phải gọi trong transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM CustomRequest r WHERE r.id = :id")
    Optional<CustomRequest> findByIdForUpdate(@Param("id") Long id);

    // Danh sách cho Staff: nạp sẵn user để tránh N+1 khi hiển thị tên khách
    @EntityGraph(attributePaths = "user")
    Page<CustomRequest> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<CustomRequest> findWithUserByStatus(CustomStatus status, Pageable pageable);

=======
>>>>>>> origin/main
    // Quy ước: không xóa vật lý custom_requests
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(CustomRequest entity) { throw new UnsupportedOperationException(); }
}
