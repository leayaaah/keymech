package vn.edu.iuh.keymech.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    List<Category> findByStatus(VisibilityStatus status, Sort sort);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Category c where c.id = :id")
    Optional<Category> lockById(long id);

    // Quy ước: không xóa vật lý categories
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(Category entity) { throw new UnsupportedOperationException(); }
}
