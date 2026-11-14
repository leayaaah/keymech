package vn.edu.iuh.keymech.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.Product;

@SpringBootTest
@Transactional
class OrderStockRepositoryTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderStockRepository orderStockRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("TC-PAY-STOCK-01: chỉ trừ tồn khi số lượng dương và đủ hàng")
    void decreaseStock_nonPositiveOrExcessiveQuantity_doesNotChangeStock() {
        String suffix = UUID.randomUUID().toString();
        Category category = new Category();
        category.setName("Nhóm thử tồn kho " + suffix);
        category.setSlug("stock-test-" + suffix);
        categoryRepository.save(category);

        Product product = new Product();
        product.setCategory(category);
        product.setName("Sản phẩm thử tồn kho " + suffix);
        product.setPrice(BigDecimal.ONE);
        product.setStockQuantity(5);
        Long productId = productRepository.saveAndFlush(product).getId();
        entityManager.clear();

        assertEquals(0, orderStockRepository.decreaseStock(productId, -2));
        assertEquals(0, orderStockRepository.decreaseStock(productId, 0));
        assertEquals(0, orderStockRepository.decreaseStock(productId, 6));
        assertEquals(1, orderStockRepository.decreaseStock(productId, 2));

        entityManager.clear();
        assertEquals(3, productRepository.findById(productId).orElseThrow().getStockQuantity());
    }
}
