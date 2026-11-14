package vn.edu.iuh.keymech.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);

    // Quy ước: không xóa vật lý categories
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(Category entity) { throw new UnsupportedOperationException(); }
}
