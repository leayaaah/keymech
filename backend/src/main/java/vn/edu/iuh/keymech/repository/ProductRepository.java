package vn.edu.iuh.keymech.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByStatus(VisibilityStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, VisibilityStatus status, Pageable pageable);

    /**
     * Lọc động theo nhiều tiêu chí (null = không lọc theo trường đó).
     * brand, layout, minPrice, maxPrice, keyword — trước đây chỉ nhận nhưng không áp dụng.
     */
    static Specification<Product> withFilters(Long categoryId, String brand, String layout,
                                              BigDecimal minPrice, BigDecimal maxPrice, String keyword) {
        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), VisibilityStatus.ACTIVE));

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(root.get("brand"), brand));
            }
            if (layout != null && !layout.isBlank()) {
                predicates.add(cb.equal(root.get("layout"), layout));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), kw);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), kw);
                predicates.add(cb.or(nameMatch, descMatch));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

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
