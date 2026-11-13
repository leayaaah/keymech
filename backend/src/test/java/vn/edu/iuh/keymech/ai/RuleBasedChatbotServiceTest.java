package vn.edu.iuh.keymech.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.ObjectMapper;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.repository.ChatProductRepository;

class RuleBasedChatbotServiceTest {

    private final ChatProductRepository products = org.mockito.Mockito.mock(ChatProductRepository.class);
    private final RuleBasedChatbotService chatbot = new RuleBasedChatbotService(products, new ObjectMapper());

    @Test
    @DisplayName("TC-BOT-08: phím 75% dưới 2 triệu chỉ lấy sản phẩm phù hợp từ CSDL")
    void reply_layoutAndBudget_queriesActiveProducts() {
        Product product = new Product();
        product.setId(5L);
        product.setName("Bàn phím 75%");
        product.setPrice(BigDecimal.valueOf(1_800_000));
        product.setStockQuantity(2);
        when(products.recommend(eq("75%"), eq(BigDecimal.valueOf(2_000_000)), eq(null), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(product)));

        var response = chatbot.reply("Phím 75% dưới 2 triệu");

        verify(products).recommend(eq("75%"), eq(BigDecimal.valueOf(2_000_000)), eq(null), any(Pageable.class));
        assertEquals("rule", response.mode());
        assertEquals(1, response.products().size());
        assertEquals(BigDecimal.valueOf(1_800_000), response.products().getFirst().price());
        assertEquals("/products/5", response.products().getFirst().url());
    }

    @Test
    @DisplayName("TC-BOT-15: câu hỏi ngoài phạm vi không truy vấn sản phẩm")
    void reply_weatherQuestion_returnsOutOfScope() {
        var response = chatbot.reply("Hôm nay có mưa không?");
        assertTrue(response.answer().contains("chỉ tư vấn bàn phím"));
        verify(products, never()).recommend(any(), any(), any(), any());
    }
}
