package vn.edu.iuh.keymech.repository;

import java.util.Optional;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.User;
=======
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.User;
import vn.edu.iuh.keymech.model.enums.Role;
>>>>>>> origin/main

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

<<<<<<< HEAD
=======
    Page<User> findByFullNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByFullNameContainingIgnoreCaseAndRole(String keyword, Role role, Pageable pageable);

>>>>>>> origin/main
    // Quy ước: không xóa vật lý users
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(User entity) { throw new UnsupportedOperationException(); }
}
