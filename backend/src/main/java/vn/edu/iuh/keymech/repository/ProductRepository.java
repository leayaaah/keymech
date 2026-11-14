package vn.edu.iuh.keymech.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStatus(VisibilityStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, VisibilityStatus status, Pageable pageable);

    /**
     * Trừ tồn kho có điều kiện: chỉ trừ khi đủ hàng VÀ sản phẩm đang ACTIVE.
     * Trả về số dòng bị ảnh hưởng (0 = không đủ hàng hoặc đã ẩn).
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty " +
           "WHERE p.id = :id AND p.stockQuantity >= :qty AND p.status = vn.edu.iuh.keymech.model.enums.VisibilityStatus.ACTIVE")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    /**
     * Hoàn tồn kho khi hủy đơn.
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :qty WHERE p.id = :id")
    int restoreStock(@Param("id") Long id, @Param("qty") int qty);

    // Quy ước: không xóa vật lý products
    @Override default void deleteById(Long id) { throw new UnsupportedOperationException(); }
    @Override default void delete(Product entity) { throw new UnsupportedOperationException(); }
}
