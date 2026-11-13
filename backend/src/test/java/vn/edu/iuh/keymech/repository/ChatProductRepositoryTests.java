package vn.edu.iuh.keymech.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;

@SpringBootTest
@Transactional
class ChatProductRepositoryTests {

    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired ChatProductRepository chatProducts;
    @Autowired EntityManager entityManager;

    @Test
    void recommendsOnlyVisibleMatchingProducts() {
        String suffix = UUID.randomUUID().toString();
        Category category = new Category();
        category.setName("Chat test " + suffix);
        category.setSlug("chat-test-" + suffix);
        categories.save(category);

        Product eligible = product(category, "Chat eligible " + suffix, 1_500_000, VisibilityStatus.ACTIVE);
        Product hidden = product(category, "Chat hidden " + suffix, 1_200_000, VisibilityStatus.HIDDEN);
        products.saveAndFlush(eligible);
        products.saveAndFlush(hidden);
        entityManager.clear();

        var result = chatProducts.recommend("75%", BigDecimal.valueOf(2_000_000), suffix, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals(eligible.getId(), result.getContent().getFirst().getId());
        assertEquals(0, chatProducts.recommend("TKL", null, suffix, PageRequest.of(0, 10)).getTotalElements());
    }

    private Product product(Category category, String name, int price, VisibilityStatus status) {
        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStockQuantity(2);
        product.setLayout("75%");
        product.setStatus(status);
        return product;
    }
}
