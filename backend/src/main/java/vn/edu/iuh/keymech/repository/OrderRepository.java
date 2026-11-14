package vn.edu.iuh.keymech.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    Optional<Order> findByOrderCode(String orderCode);

    // Quy ước: không xóa vật lý orders
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(Order entity) { throw new UnsupportedOperationException(); }
=======
import org.springframework.data.repository.Repository;
import vn.edu.iuh.keymech.model.Order;

// Chỉ công khai các thao tác cần dùng; orders không được xóa vật lý.
public interface OrderRepository extends Repository<Order, Long> {
    <S extends Order> S save(S order);
    Optional<Order> findById(Long id);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    Optional<Order> findByOrderCode(String orderCode);
>>>>>>> origin/main
}
