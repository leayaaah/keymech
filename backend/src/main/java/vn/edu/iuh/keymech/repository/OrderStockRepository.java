package vn.edu.iuh.keymech.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.keymech.model.Product;

/** Thao tác tồn kho dùng trong giao dịch đặt hàng. */
public interface OrderStockRepository extends Repository<Product, Long> {

    /** Trả về 1 khi trừ thành công, 0 khi số lượng không dương, thiếu hàng hoặc sản phẩm đã ẩn. */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty " +
           "WHERE p.id = :id AND :qty > 0 AND p.stockQuantity >= :qty " +
           "AND p.status = vn.edu.iuh.keymech.model.enums.VisibilityStatus.ACTIVE")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);
}
