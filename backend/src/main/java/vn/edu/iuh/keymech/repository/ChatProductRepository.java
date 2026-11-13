package vn.edu.iuh.keymech.repository;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.keymech.model.Product;

/** Truy vấn riêng của Chatbot; không sửa ProductRepository do module Catalog sở hữu. */
public interface ChatProductRepository extends Repository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.status = vn.edu.iuh.keymech.model.enums.VisibilityStatus.ACTIVE " +
           "AND p.category.status = vn.edu.iuh.keymech.model.enums.VisibilityStatus.ACTIVE " +
           "AND (:layout IS NULL OR lower(p.layout) = lower(:layout)) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:term IS NULL OR lower(p.name) LIKE lower(concat('%', :term, '%')) " +
           "OR lower(p.brand) LIKE lower(concat('%', :term, '%')) " +
           "OR lower(p.description) LIKE lower(concat('%', :term, '%')) " +
           "OR lower(p.specs) LIKE lower(concat('%', :term, '%')))")
    Page<Product> recommend(@Param("layout") String layout,
                            @Param("maxPrice") BigDecimal maxPrice,
                            @Param("term") String term, Pageable pageable);
}
