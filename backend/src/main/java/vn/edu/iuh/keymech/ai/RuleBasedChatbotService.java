package vn.edu.iuh.keymech.ai;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
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
import vn.edu.iuh.keymech.repository.ChatProductRepository;

@Service
public class RuleBasedChatbotService implements ChatbotService {

    private static final Pattern LAYOUT = Pattern.compile("(?<!\\w)(full[- ]?size|100%|tkl|75%|65%|60%|40%)(?!\\w)");
    private static final Pattern BUDGET = Pattern.compile(
        "(?<![\\w.,])(?:(\\d+(?:[.,]\\d+)?)\\s*(trieu|tr|nghin|k)|(\\d{1,3}(?:[.,]\\d{3})+|\\d{4,})"
            + "(?:\\s*(?:vnd|dong|d))?)(?![\\w%]|[.,]\\d)");
    private static final List<String> TERMS = List.of(
        "keychron", "gateron", "filco", "ducky", "akko", "linear", "tactile",
        "clicky", "keycap", "switch", "kit");
    private static final List<String> SUGGESTIONS = List.of(
        "Phím 75% dưới 2 triệu", "Switch linear", "Tư vấn Custom");
    private static final Pattern HOT_SWAP = Pattern.compile("\\bhot[\\s-]*swap(?:pable|able)?\\b");
    private static final Pattern SOLDERED = Pattern.compile("\\b(?:mach han|pcb han|han switch|switch han|solder(?:ed)?)\\b");
    private static final Pattern PIN_COUNT = Pattern.compile("\\b[35]\\s*-?\\s*(?:chan|pins?)\\b");
    private static final Pattern KEYCAP = Pattern.compile("\\bkeycaps?\\b");
    private static final Pattern SWITCH = Pattern.compile("\\bswitch(?:es)?\\b");
    private static final Pattern COMPATIBILITY = Pattern.compile(
        "\\b(?:tuong thich|khop|co vua|vua voi|co hop|(?:lap|gan|dung|thay|vua)\\b.{0,85}\\b(?:duoc|khong))\\b");
    private static final Pattern EXPLANATION = Pattern.compile(
        "\\b(?:la gi|la sao|dung de lam gi|khac nhau|phan biet|cach chon|chon the nao|chon nhu the nao)\\b");
    private static final Pattern SHOPPING = Pattern.compile(
        "\\b(?:mua|tim (?:ban phim|phim|switch|keycap)|goi y|gia bao nhieu|bao nhieu tien|ngan sach|duoi)\\b");
    private static final Pattern CUSTOM_BUILD = Pattern.compile(
        "\\b(?:custom|lap rap|tu lap|tu build|build (?:phim|ban phim)|(?:phim|ban phim) theo yeu cau)\\b");
    private static final Pattern QUIET_TYPING = Pattern.compile(
        "\\b(?:go em|go yen tinh|it on|bot on|giam on|it tieng|silent)\\b");

    private final ChatProductRepository products;
    private final CustomComponentRecommendations customComponents;
    private final Map<String, String> intents;

    public RuleBasedChatbotService(ChatProductRepository products, ObjectMapper mapper) {
        this.products = products;
        this.customComponents = new CustomComponentRecommendations(mapper);
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
        if (containsAny(text, "thoi tiet", "troi mua", "hom nay co mua", "bong da", "system prompt")) {
            return outOfScope();
        }
        ChatResponse advice = componentAdvice(text);
        if (advice != null) return advice;
        if (CUSTOM_BUILD.matcher(text).find()) {
            return response("custom", List.of());
        }
        if (containsAny(text, "bao hanh", "doi tra", "van chuyen", "giao hang")) {
            return response("policy", List.of());
        }
        String layout = layout(text);
        BigDecimal maxPrice = budget(text);
        String term = TERMS.stream().filter(text::contains).findFirst().orElse(null);
        boolean shoppingQuestion = layout != null || maxPrice != null || term != null
            || containsAny(text, "ban phim", "goi y", "tu van", "san pham", "gia", "mua", "phu kien");
        if (!shoppingQuestion) {
            return outOfScope();
        }
        List<ChatProductResponse> matches = products.recommend(
                layout, maxPrice, term,
                PageRequest.of(0, 6, Sort.by("price").ascending().and(Sort.by("id"))))
            .getContent().stream().map(ChatProductResponse::from).toList();
        return response(matches.isEmpty() ? "noResults" : "recommend", matches);
    }

    private ChatResponse response(String key, List<ChatProductResponse> matches) {
        return new ChatResponse(intents.get(key), matches, SUGGESTIONS, "rule");
    }

    ChatResponse outOfScope() {
        return response("outOfScope", List.of());
    }

    ChatResponse componentAdvice(String text) {
        if (CUSTOM_BUILD.matcher(text).find() && !EXPLANATION.matcher(text).find()) {
            ChatResponse build = customBuildAdvice(text);
            if (build != null) return build;
        }
        return compatibilityFaq(text);
    }

    private ChatResponse customBuildAdvice(String text) {
        String requestedLayout = layout(text);
        BigDecimal requestedBudget = budget(text);
        boolean quiet = QUIET_TYPING.matcher(text).find();
        if (requestedLayout == null && requestedBudget == null && !quiet
                && !containsAny(text, "cau hinh", "tung linh kien", "chon linh kien")) {
            return null;
        }

        String layoutLabel = requestedLayout == null ? "theo layout bạn chọn" : "layout " + requestedLayout;
        String budgetLabel = requestedBudget == null ? "" : " với ngân sách "
            + CustomComponentRecommendations.money(requestedBudget);
        var choice = customComponents.recommend(products.findPublicProducts(), requestedLayout, requestedBudget, quiet);
        String answer = intents.get("customBuild").formatted(layoutLabel, budgetLabel, choice.kit(), choice.pcb(),
            choice.switches(), choice.stabilizer(), choice.keycap(), choice.total());
        return new ChatResponse(answer, choice.products(), SUGGESTIONS, "rule");
    }

    private ChatResponse compatibilityFaq(String text) {
        boolean compatible = COMPATIBILITY.matcher(text).find();
        boolean explanation = EXPLANATION.matcher(text).find();
        if (!compatible && !explanation && SHOPPING.matcher(text).find()) return null;
        if (KEYCAP.matcher(text).find() && (compatible || explanation || text.equals("keycap"))) {
            return response(compatible ? "keycapCompatibility" : "keycapFaq", List.of());
        }
        if (compatible && SWITCH.matcher(text).find()
                && containsAny(text, "switch nay", "switch do", "tuong thich", "lap", "gan", "dung")) {
            return response("switchCompatibility", List.of());
        }
        if (PIN_COUNT.matcher(text).find()) return response("switchPinsFaq", List.of());
        if (SOLDERED.matcher(text).find() || HOT_SWAP.matcher(text).find()) return response("hotSwapFaq", List.of());
        if (compatible && SWITCH.matcher(text).find()) return response("switchCompatibility", List.of());
        if (compatible && containsAny(text, "linh kien", "phu kien", "custom")) {
            return response("componentCompatibility", List.of());
        }
        return null;
    }

    static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").replace('đ', 'd');
    }

    private static boolean containsAny(String text, String... phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    static String layout(String text) {
        Matcher match = LAYOUT.matcher(text);
        if (!match.find()) return null;
        return switch (match.group(1)) {
            case "100%", "fullsize", "full-size", "full size" -> "Fullsize";
            case "tkl" -> "TKL";
            default -> match.group(1);
        };
    }

    static BigDecimal budget(String text) {
        Matcher match = BUDGET.matcher(text);
        if (!match.find()) return null;
        String unit = match.group(2);
        if (unit == null) {
            return new BigDecimal(match.group(3).replace(".", "").replace(",", ""));
        }
        BigDecimal value = new BigDecimal(match.group(1).replace(',', '.'));
        return value.multiply(unit.equals("trieu") || unit.equals("tr")
            ? BigDecimal.valueOf(1_000_000) : BigDecimal.valueOf(1_000));
    }
}
