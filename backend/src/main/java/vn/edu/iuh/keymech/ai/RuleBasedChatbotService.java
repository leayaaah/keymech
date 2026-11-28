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
import vn.edu.iuh.keymech.dto.ChatContext;
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
    private static final Pattern SWITCH_TYPE = Pattern.compile(
        "\\b(?:red|brown|blue|black|yellow|silver|linear|tactile|clicky|silent)\\b");
    private static final Pattern EXPLANATION = Pattern.compile(
        "\\b(?:la gi|la sao|dung de lam gi|so sanh|phan biet|cach chon|chon the nao|chon nhu the nao"
            + "|khac\\s+(?:nhau|voi|cho nao|diem nao|the nao|nhu the nao|gi|sao)"
            + "|khac\\s+(?:switch\\s+)?" + SWITCH_TYPE.pattern() + ")\\b");
    private static final Pattern SHOPPING = Pattern.compile(
        "\\b(?:mua|tim (?:ban phim|phim|switch|keycap)|goi y|gia bao nhieu|bao nhieu tien|ngan sach|duoi)\\b");
    private static final Pattern CUSTOM_BUILD = Pattern.compile(
        "\\b(?:custom|lap rap|tu lap|tu build|build (?:phim|ban phim)|(?:phim|ban phim) theo yeu cau)\\b");
    private static final Pattern QUIET_TYPING = Pattern.compile(
        "\\b(?:go em|go yen tinh|it on|bot on|giam on|it tieng|silent)\\b");
    private static final Pattern NO_QUIET = Pattern.compile(
        "\\b(?:khong can|khong muon|khong phai)\\s+(?:go em|silent|yen tinh)\\b");
    private static final Pattern SWITCH_NAME = Pattern.compile("\\b" + ChatContext.SWITCH_NAME + "\\b");
    private static final Pattern RESET_SWITCH = Pattern.compile("\\b(?:switch nao cung duoc|khong can switch co dinh)\\b");
    private static final Pattern FOLLOW_UP_WORDS = Pattern.compile(
        "\\b(?:ngan sach|co khong|duoc khong|co duoc khong|thi sao|switch(?:es)?|layout|"
            + "doi sang|doi thanh|muon|chon|chi|toi da|tam|khoang|duoi|toi|con|co|khong|duoc|nhe|a|voi|minh|la)\\b");
    private static final Pattern ACCESSORY = Pattern.compile(
        "\\b(?:phu kien|ke tay|cap(?: sac| usb(?:[ -]c)?)|dung cu|puller|co rut|lube|stabilizer|pcb)\\b");
    private static final Pattern ACCESSORY_REQUEST = Pattern.compile(
        "(?:^|\\b(?:mua|tim|goi y|tu van)\\s+)" + ACCESSORY.pattern());
    private static final Pattern KEYBOARD = Pattern.compile("\\b(?:ban phim|phim|keyboard)\\b");
    private static final Pattern KIT = Pattern.compile("\\bkit\\b");

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
    public ChatResponse reply(String question, ChatContext context) {
        String text = normalize(question).strip();
        if (containsAny(text, "xin chao", "chao ban", "hello")) {
            return response("greeting", List.of());
        }
        if (containsAny(text, "thoi tiet", "troi mua", "hom nay co mua", "bong da", "system prompt")) {
            return outOfScope();
        }
        ChatResponse advice = componentAdvice(text, context);
        if (advice != null) return advice;
        String layout = layout(text);
        BigDecimal maxPrice = budget(text);
        if (isExplanation(text) && layout == null && maxPrice == null && !SHOPPING.matcher(text).find()) {
            return clarify();
        }
        String category = catalogCategory(text);
        String term = catalogTerm(text, category);
        if (category == null && layout == null && term == null) return clarify();
        List<ChatProductResponse> matches = products.recommend(
                layout, maxPrice, term, category,
                PageRequest.of(0, 6, Sort.by("price").ascending().and(Sort.by("id"))))
            .getContent().stream().map(ChatProductResponse::from).toList();
        return catalogResults(matches);
    }

    private ChatResponse response(String key, List<ChatProductResponse> matches) {
        return new ChatResponse(intents.get(key), matches, SUGGESTIONS, "rule", null, key);
    }

    ChatResponse outOfScope() {
        return response("outOfScope", List.of());
    }

    ChatResponse clarify() {
        return response("clarify", List.of());
    }

    ChatResponse catalogResults(List<ChatProductResponse> matches) {
        return response(matches.isEmpty() ? "noResults" : "recommend", matches);
    }

    ChatResponse componentAdvice(String text, ChatContext context) {
        ChatResponse followUp = followUp(text, context);
        if (followUp != null) return followUp;
        boolean custom = CUSTOM_BUILD.matcher(text).find();
        ChatResponse faq = compatibilityFaq(text);
        if (faq == null && isExplanation(text) && SWITCH_TYPE.matcher(text).find()) {
            return response("switchTypeFaq", List.of());
        }
        if (custom) {
            var slots = slots(text);
            if (context != null && followUpText(CUSTOM_BUILD.matcher(text).replaceAll(" "))) {
                slots = merge(context, slots, text);
            }
            if (slots.layout() != null && !EXPLANATION.matcher(text).find()) return customBuildAdvice(slots);
            if (faq != null) return faq;
            return customPrompt(slots);
        }
        if (faq != null) return faq;
        String requestedSwitch = switchName(text);
        if (requestedSwitch != null && "switch".equals(catalogCategory(text)) && !isExplanation(text)) {
            BigDecimal maxPrice = budget(text);
            return catalogResults(products.findPublicProducts().stream()
                .filter(p -> "switch".equals(p.getCategory().getSlug()) && matchesSwitch(p.getName(), requestedSwitch))
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .limit(6).map(ChatProductResponse::from).toList());
        }
        return containsAny(text, "bao hanh", "doi tra", "van chuyen", "giao hang")
            ? response("policy", List.of()) : null;
    }

    private ChatResponse followUp(String text, ChatContext context) {
        if (context == null || !followUpText(text)) return null;
        var update = slots(text);
        if (update.layout() == null && update.budget() == null && update.quiet() == null
                && update.switchName() == null && !RESET_SWITCH.matcher(text).find()) return null;
        var merged = merge(context, update, text);
        return merged.layout() == null ? customPrompt(merged) : customBuildAdvice(merged);
    }

    private ChatResponse customPrompt(ChatContext slots) {
        return new ChatResponse(intents.get("custom"), List.of(), SUGGESTIONS, "rule", slots, "custom");
    }

    private static boolean followUpText(String text) {
        String remaining = BUDGET.matcher(LAYOUT.matcher(text).replaceAll(" ")).replaceAll(" ");
        remaining = NO_QUIET.matcher(remaining).replaceAll(" ");
        remaining = QUIET_TYPING.matcher(remaining).replaceAll(" ");
        remaining = SWITCH_NAME.matcher(RESET_SWITCH.matcher(remaining).replaceAll(" ")).replaceAll(" ");
        return FOLLOW_UP_WORDS.matcher(remaining).replaceAll(" ").replaceAll("[\\p{Punct}\\s]+", "").isEmpty();
    }

    private static ChatContext slots(String text) {
        Boolean quiet = null;
        if (NO_QUIET.matcher(text).find()) quiet = false;
        else if (QUIET_TYPING.matcher(text).find()) quiet = true;
        return new ChatContext(layout(text), budget(text), quiet, switchName(text));
    }

    private static ChatContext merge(ChatContext previous, ChatContext update, String text) {
        return new ChatContext(update.layout() == null ? previous.layout() : update.layout(),
            update.budget() == null ? previous.budget() : update.budget(),
            update.quiet() == null ? previous.quiet() : update.quiet(),
            RESET_SWITCH.matcher(text).find() ? null
                : update.switchName() == null ? previous.switchName() : update.switchName());
    }

    private ChatResponse customBuildAdvice(ChatContext context) {
        String layoutLabel = "layout " + context.layout();
        String budgetLabel = context.budget() == null ? "" : " với ngân sách "
            + CustomComponentRecommendations.money(context.budget());
        var choice = customComponents.recommend(products.findPublicProducts(), context);
        String answer = intents.get("customBuild").formatted(layoutLabel, budgetLabel, choice.kit(), choice.pcb(),
            choice.switches(), choice.stabilizer(), choice.keycap(), choice.total());
        return new ChatResponse(answer, choice.products(), SUGGESTIONS, "rule", context,
            choice.overBudget() ? "customAlternatives" : "customBuild");
    }

    static String catalogCategory(String text) {
        if (ACCESSORY_REQUEST.matcher(text).find()
                || (ACCESSORY.matcher(text).find() && !KEYBOARD.matcher(text).find())) return "dung-cu-phu-kien";
        if (KIT.matcher(text).find()) return "kit-ban-phim";
        if (KEYBOARD.matcher(text).find()) return "ban-phim-co";
        if (SWITCH.matcher(text).find()) return "switch";
        if (KEYCAP.matcher(text).find()) return "keycap";
        return layout(text) == null ? null : "ban-phim-co";
    }

    private static String catalogTerm(String text, String category) {
        if ("dung-cu-phu-kien".equals(category)) {
            var match = ACCESSORY.matcher(text);
            if (match.find() && !List.of("phu kien", "dung cu").contains(match.group())) return match.group();
        }
        return TERMS.stream().filter(text::contains).findFirst().orElse(null);
    }

    static boolean isExplanation(String text) {
        return EXPLANATION.matcher(text).find();
    }

    private static String switchName(String text) {
        var matcher = SWITCH_NAME.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    static boolean matchesSwitch(String name, String requested) {
        String normalized = normalize(name).replaceAll("[^a-z0-9]+", " ").strip();
        return Pattern.compile("\\b" + String.join("(?: \\w+)* ", requested.split(" ")) + "\\b")
            .matcher(normalized).find();
    }

    private ChatResponse compatibilityFaq(String text) {
        boolean compatible = COMPATIBILITY.matcher(text).find();
        boolean explanation = isExplanation(text);
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
        BigDecimal value = unit == null ? new BigDecimal(match.group(3).replace(".", "").replace(",", ""))
            : new BigDecimal(match.group(1).replace(',', '.'))
                .multiply(BigDecimal.valueOf(unit.equals("trieu") || unit.equals("tr") ? 1_000_000 : 1_000));
        value = value.stripTrailingZeros();
        return value.precision() - value.scale() <= 18 && value.scale() <= 2
            ? value.setScale(Math.max(0, value.scale())) : null;
    }
}
