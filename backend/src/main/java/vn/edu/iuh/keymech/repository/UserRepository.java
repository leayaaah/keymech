package vn.edu.iuh.keymech.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Quy ước: không xóa vật lý users
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(User entity) { throw new UnsupportedOperationException(); }
}
