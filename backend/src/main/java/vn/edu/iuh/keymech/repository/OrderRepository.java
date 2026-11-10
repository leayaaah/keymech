package vn.edu.iuh.keymech.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    Optional<Order> findByOrderCode(String orderCode);

    // Quy ước: không xóa vật lý orders
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(Order entity) { throw new UnsupportedOperationException(); }
}
