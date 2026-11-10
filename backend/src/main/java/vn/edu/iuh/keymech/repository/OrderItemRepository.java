package vn.edu.iuh.keymech.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.keymech.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}
