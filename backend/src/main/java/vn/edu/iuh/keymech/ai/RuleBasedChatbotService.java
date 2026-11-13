package vn.edu.iuh.keymech.ai;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import vn.edu.iuh.keymech.dto.ChatProductResponse;
import vn.edu.iuh.keymech.dto.ChatResponse;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.repository.ChatProductRepository;

@Service
public class RuleBasedChatbotService implements ChatbotService {

    private static final Pattern LAYOUT = Pattern.compile("(?<!\\w)(full[- ]?size|100%|tkl|75%|65%|60%|40%)(?!\\w)");
    private static final Pattern BUDGET = Pattern.compile("(?<!\\d)(\\d+(?:[.,]\\d+)?)\\s*(trieu|tr|k|nghin)(?!\\w)");
    private static final List<String> TERMS = List.of(
        "keychron", "gateron", "filco", "ducky", "akko", "linear", "tactile",
        "clicky", "keycap", "switch", "kit");
    private static final List<String> SUGGESTIONS = List.of(
        "Phím 75% dưới 2 triệu", "Switch linear", "Tư vấn Custom");

    private final ChatProductRepository products;
    private final Map<String, String> intents;

    public RuleBasedChatbotService(ChatProductRepository products, ObjectMapper mapper) {
        this.products = products;
        try (InputStream input = new ClassPathResource("chatbot/chatbot-intents.json").getInputStream()) {
            this.intents = mapper.readValue(input, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Không đọc được mẫu trả lời Chatbot", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse reply(String question) {
        String text = normalize(question);
        if (containsAny(text, "xin chao", "chao ban", "hello")) {
            return response("greeting", List.of());
        }
        if (containsAny(text, "custom", "lap rap", "tu lap")) {
            return response("custom", List.of());
        }
        if (containsAny(text, "bao hanh", "doi tra", "van chuyen", "giao hang")) {
            return response("policy", List.of());
        }
        if (containsAny(text, "thoi tiet", "troi mua", "hom nay co mua", "bong da", "system prompt")) {
            return response("outOfScope", List.of());
        }

        String layout = layout(text);
        BigDecimal maxPrice = budget(text);
        String term = TERMS.stream().filter(text::contains).findFirst().orElse(null);
        boolean shoppingQuestion = layout != null || maxPrice != null || term != null
            || containsAny(text, "ban phim", "goi y", "tu van", "san pham", "gia", "mua", "phu kien");
        if (!shoppingQuestion) {
            return response("outOfScope", List.of());
        }
        List<ChatProductResponse> matches = products.recommend(
                layout, maxPrice, term,
                PageRequest.of(0, 6, Sort.by("price").ascending().and(Sort.by("id"))))
            .getContent().stream().map(this::toResponse).toList();
        return response(matches.isEmpty() ? "noResults" : "recommend", matches);
    }

    private ChatProductResponse toResponse(Product product) {
        return new ChatProductResponse(product.getId(), product.getName(), product.getPrice(),
            product.getImageUrl(), product.getStockQuantity() > 0, "/products/" + product.getId());
    }

    private ChatResponse response(String key, List<ChatProductResponse> matches) {
        return new ChatResponse(intents.get(key), matches, SUGGESTIONS, "rule");
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value.toLowerCase(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").replace('đ', 'd');
    }

    private static boolean containsAny(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private static String layout(String text) {
        Matcher match = LAYOUT.matcher(text);
        if (!match.find()) return null;
        return switch (match.group(1)) {
            case "100%", "full-size", "full size" -> "Full-size";
            case "tkl" -> "TKL";
            default -> match.group(1);
        };
    }

    private static BigDecimal budget(String text) {
        Matcher match = BUDGET.matcher(text);
        if (!match.find()) return null;
        BigDecimal value = new BigDecimal(match.group(1).replace(',', '.'));
        return value.multiply(match.group(2).equals("trieu") || match.group(2).equals("tr")
            ? BigDecimal.valueOf(1_000_000) : BigDecimal.valueOf(1_000));
    }
}
