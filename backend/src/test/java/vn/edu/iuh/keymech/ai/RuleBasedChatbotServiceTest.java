package vn.edu.iuh.keymech.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.ObjectMapper;
import vn.edu.iuh.keymech.dto.ChatProductResponse;
import vn.edu.iuh.keymech.model.Category;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.repository.ChatProductRepository;

class RuleBasedChatbotServiceTest {

    private final ChatProductRepository products = mock(ChatProductRepository.class);
    private final RuleBasedChatbotService chatbot = new RuleBasedChatbotService(products, new ObjectMapper());

    @Test
    @DisplayName("TC-BOT-08: phím 75% dưới 2 triệu chỉ lấy sản phẩm phù hợp từ CSDL")
    void reply_layoutAndBudget_queriesActiveProducts() {
        Product product = new Product();
        product.setId(5L);
        product.setName("Bàn phím 75%");
        product.setPrice(BigDecimal.valueOf(1_800_000));
        product.setStockQuantity(2);
        when(products.recommend(eq("75%"), eq(BigDecimal.valueOf(2_000_000)), eq(null), eq("ban-phim-co"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(product)));

        var response = chatbot.reply("Phím 75% dưới 2 triệu");

        verify(products).recommend(eq("75%"), eq(BigDecimal.valueOf(2_000_000)), eq(null), eq("ban-phim-co"), any(Pageable.class));
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
        verify(products, never()).recommend(any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @DisplayName("FAQ linh kiện trả lời ở cả hai chế độ mà không gọi Gemini")
    @CsvSource({
        "Mạch hàn là sao?, tháo mối hàn cũ",
        "Mach han co thay switch duoc khong?, tháo mối hàn cũ",
        "Hot-swap là gì?, socket",
        "Switch 3/5 chân khác nhau thế nào?, hai lỗ tương ứng",
        "Keycap là gì?, stem",
        "Switch này lắp được vào bàn phím của mình không?, model và phiên bản bàn phím hoặc PCB",
        "Bộ keycap này có vừa layout 75% không?, kiểu stem của switch và bố trí phím thực tế",
        "Làm sao kiểm tra tương thích linh kiện Custom?, Bạn muốn kiểm tra switch hay keycap?"
    })
    void reply_componentFaq_matchesInBothModes(String question, String expected) {
        var executor = mock(ExecutorService.class);
        var ai = new AiChatbotService(chatbot, products, null, null, executor, 8);

        var ruleResponse = chatbot.reply(question);
        assertTrue(ruleResponse.answer().contains(expected), ruleResponse.answer());
        assertEquals("rule", ruleResponse.mode());
        assertTrue(ruleResponse.products().isEmpty());
        assertEquals(ruleResponse, ai.reply(question));
        verifyNoInteractions(products, executor);
    }

    @Test
    @DisplayName("Tư vấn Custom 75% dưới 2 triệu từ danh mục đang bán")
    void reply_quietCustomBuild_suggestsComponentsInBothModes() {
        var executor = mock(ExecutorService.class);
        var ai = new AiChatbotService(chatbot, products, null, null, executor, 8);
        String question = "mình muốn custom phím cơ dưới 2tr layout 75% gõ êm chút";
        String board = """
            {"keys":84,"includes":"Case, PCB, Plate","platform":"Test75","switchFamily":"MX",
             "supportedPins":[3,5],"stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"1.75u"}
            """;
        String silent = """
            {"pack":35,"pinCount":5,"switchFamily":"MX","silent":true}
            """;
        when(products.findPublicProducts()).thenReturn(List.of(
            part(1, "kit-ban-phim", "Kit 75 mẫu", 850_000, 10, "75%", null, board),
            part(2, "dung-cu-phu-kien", "PCB rời trùng kit", 200_000, 5, "75%", null,
                "{\"componentType\":\"pcb\",\"platform\":\"Test75\"}"),
            part(3, "switch", "Silent Linear mẫu", 205_000, 3, null, "Linear", silent),
            part(4, "switch", "Silent hết hàng", 100_000, 0, null, "Linear", silent),
            part(5, "switch", "Silent thiếu gói", 105_000, 2, null, "Linear", silent),
            part(6, "dung-cu-phu-kien", "Stabilizer mẫu", 140_000, 5, "75%", null,
                "{\"componentType\":\"stabilizer\",\"platform\":\"Test75\"}"),
            part(7, "keycap", "Bộ keycap mẫu", 265_000, 5, "75%", null,
                "{\"stem\":\"MX\",\"physicalLayout\":\"ANSI\",\"spacebarWidth\":\"6.25u\",\"rightShiftWidth\":\"1.75u\"}"),
            part(8, "keycap", "Bộ keycap ISO", 180_000, 8, "75%", null, "{\"physicalLayout\":\"ISO\"}"),
            part(9, "kit-ban-phim", "Kit layout khác", 500_000, 5, "65%", null, board),
            part(10, "switch", "Silent không tương thích", 100_000, 9, null, "Linear",
                "{\"pack\":35,\"switchFamily\":\"Optical\",\"silent\":true}")
        ));

        var response = chatbot.reply(question);

        for (String expected : List.of("layout 75%", "2.000.000 ₫", "Kit 75 mẫu", "PCB đi kèm kit",
                "Silent Linear mẫu", "Cần 3 gói", "Stabilizer mẫu", "Bộ keycap mẫu", "1.870.000 ₫")) {
            assertTrue(response.answer().contains(expected), response.answer());
        }
        assertEquals(List.of(1L, 3L, 6L, 7L), response.products().stream().map(ChatProductResponse::id).toList());
        assertEquals(List.of(1, 3, 1, 1), response.products().stream().map(ChatProductResponse::quantity).toList());
        assertEquals(response, ai.reply(question));
        verifyNoInteractions(executor);
    }

    @Test
    void reply_customBuildWhenCatalogLacksParts_reportsGaps() {
        when(products.findPublicProducts()).thenReturn(List.of(
            part(1, "kit-ban-phim", "Kit quá ngân sách", 3_600_000, 7, "TKL", null, null),
            part(2, "switch", "Switch linear mẫu", 250_000, 7, null, "Linear", "{\"pack\":35}"),
            part(3, "keycap", "Keycap Artisan ESC", 175_000, 10, null, null, "{}")));
        var response = chatbot.reply("Gợi ý cấu hình tự lắp bàn phím TKL gõ êm dưới 2 triệu");

        assertTrue(response.answer().contains("layout TKL"));
        assertTrue(response.answer().contains("Kit quá ngân sách"));
        assertTrue(response.answer().contains("chưa xác nhận mẫu này là silent"));
        assertTrue(response.answer().contains("Chưa có bộ keycap"));
        assertTrue(response.answer().contains("vượt ít nhất"));
        assertTrue(response.products().isEmpty());
    }

    @Test
    void reply_customNamedSwitchNeverSubstitutesAnotherColor() {
        when(products.findPublicProducts()).thenReturn(List.of(
            part(1, "kit-ban-phim", "Kit 75 mẫu", 900_000, 2, "75%", null,
                "{\"keys\":84,\"includes\":\"PCB, Stabilizer\"}"),
            part(2, "switch", "Cherry MX Red", 240_000, 2, null, "Linear", "{\"pack\":35}"),
            part(3, "switch", "Gateron Yellow", 180_000, 9, null, "Linear", "{\"pack\":35,\"silent\":true}")));

        var prompt = chatbot.reply("Tư vấn Custom");
        assertEquals("custom", prompt.intent());
        assertTrue(prompt.products().isEmpty());
        var build = chatbot.reply("75%, gõ êm, switch Red", prompt.context());
        assertEquals("red", build.context().switchName());
        assertTrue(build.answer().contains("Chưa có switch RED"));
        assertTrue(build.answer().contains("Ngân sách bạn dự kiến"));
        assertEquals(List.of(1L), build.products().stream().map(ChatProductResponse::id).toList());
    }

    private static Product part(long id, String slug, String name, int price, int stock, String layout,
                                String switchType, String specs) {
        Category category = new Category();
        category.setSlug(slug);
        Product product = new Product();
        product.setId(id);
        product.setCategory(category);
        product.setName(name);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStockQuantity(stock);
        product.setLayout(layout);
        product.setSwitchType(switchType);
        product.setSpecs(specs);
        return product;
    }

    @Test
    void reply_customProcessQuestion_keepsServiceAnswer() {
        var response = chatbot.reply("Custom gồm những bước nào?");

        assertTrue(response.answer().contains("gửi yêu cầu Custom"));
        verifyNoInteractions(products);
    }

    @Test
    void reply_purchaseQuestionWithPins_stillLooksUpProducts() {
        when(products.recommend(eq(null), eq(BigDecimal.valueOf(500_000)), eq("switch"), eq("switch"), any(Pageable.class)))
            .thenReturn(Page.empty());

        chatbot.reply("Mua switch 5 chân dưới 500 nghìn");

        verify(products).recommend(eq(null), eq(BigDecimal.valueOf(500_000)), eq("switch"), eq("switch"), any(Pageable.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Fullsize dưới 2000000", "Full-size dưới 2.000.000đ", "Full size dưới 2,000,000 VND",
        "Phím 100% dưới 2tr"
    })
    void reply_fullsizeAliasesAndBudget_useCatalogLayout(String question) {
        when(products.recommend(eq("Fullsize"), eq(BigDecimal.valueOf(2_000_000)),
            eq(null), eq("ban-phim-co"), any(Pageable.class))).thenReturn(Page.empty());

        chatbot.reply(question);

        verify(products).recommend(eq("Fullsize"), eq(BigDecimal.valueOf(2_000_000)),
            eq(null), eq("ban-phim-co"), any(Pageable.class));
    }
}
