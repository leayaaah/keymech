package vn.edu.iuh.keymech.ai;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import vn.edu.iuh.keymech.dto.ChatProductResponse;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.util.ProductSpecifications;

final class CustomComponentRecommendations {

    private static final Pattern KEYCAP_SET = Pattern.compile("\\b(?:bo keycap|keycaps? set)\\b");
    private static final Pattern SILENT = Pattern.compile("\\bsilent\\b");
    private static final List<String> KEYCAP_SPECS = List.of("stem", "physicalLayout", "spacebarWidth", "rightShiftWidth");
    private final ObjectMapper mapper;

    CustomComponentRecommendations(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    Recommendation recommend(List<Product> catalog, String layout, BigDecimal budget, boolean quiet) {
        var available = catalog.stream().filter(product -> product.getStockQuantity() > 0)
            .filter(product -> withinBudget(product.getPrice(), budget)).map(this::component)
            .sorted(Comparator.comparing((Component part) -> part.product().getPrice())
                .thenComparing(part -> part.product().getId()))
            .toList();

        Component kit = first(available, part -> part.inCategory("kit-ban-phim") && matchesLayout(part, layout));
        boolean bundledPcb = included(kit, "PCB");
        Component pcb = bundledPcb ? null : first(available, part -> part.type("pcb")
            && matchesLayout(part, layout) && matchesPlatform(kit, part));
        Component board = pcb == null ? kit : pcb;
        boolean bundledStabilizer = included(kit, "stabilizer");
        Component stabilizer = bundledStabilizer ? null : first(available, part -> part.type("stabilizer")
            && matchesPlatform(board, part) && (matchesLayout(part, layout) || part.product().getLayout() == null));
        Component keycap = available.stream().filter(part -> part.inCategory("keycap"))
            .filter(part -> part.type("keycap-set") || KEYCAP_SET.matcher(part.name()).find())
            .filter(part -> matchesLayout(part, layout) || part.product().getLayout() == null)
            .filter(part -> KEYCAP_SPECS.stream().noneMatch(field -> conflicts(board, part, field)))
            .min(Comparator.comparing(part -> !matchesKeycapSpecs(board, part))).orElse(null);

        int keys = board == null ? 0 : board.specs().path("keys").asInt(0);
        SwitchChoice switches = available.stream().filter(part -> part.inCategory("switch"))
            .filter(part -> !quiet || part.silent() || "Linear".equalsIgnoreCase(part.product().getSwitchType())
                || "Tactile".equalsIgnoreCase(part.product().getSwitchType()))
            .filter(part -> !conflicts(board, part, "switchFamily") && supportsPins(board, part))
            .map(part -> new SwitchChoice(part, keys))
            .filter(choice -> choice.quantity() <= choice.part().product().getStockQuantity())
            .filter(choice -> withinBudget(choice.cost(), budget))
            .min(Comparator.comparing((SwitchChoice choice) -> quiet && !choice.part().silent())
                .thenComparing(SwitchChoice::cost)).orElse(null);

        String pcbText = bundledPcb ? "PCB đi kèm kit " + kit.product().getName() + "." :
            describe(pcb, "Chưa có PCB rời phù hợp trong danh mục đang bán.");
        String stabilizerText = bundledStabilizer ? "Stabilizer đi kèm kit " + kit.product().getName() + "." :
            describe(stabilizer, "Chưa có bộ stabilizer rời phù hợp trong danh mục đang bán.");
        String keycapText = describe(keycap, "Chưa có bộ keycap phù hợp trong danh mục đang bán.")
            + (keycap != null && !matchesKeycapSpecs(board, keycap) ? " Cần đối chiếu stem và bố trí phím thực tế." : "");

        List<ChatProductResponse> cards = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Component part : Stream.of(kit, pcb, switches == null ? null : switches.part(), stabilizer, keycap)
                .filter(candidate -> candidate != null).toList()) {
            int quantity = switches != null && part == switches.part() ? switches.quantity() : 1;
            cards.add(ChatProductResponse.from(part.product(), quantity));
            subtotal = subtotal.add(part.product().getPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        String total = "";
        if (!cards.isEmpty() && (switches == null || switches.quantityKnown())) {
            total = "Tạm tính các món có thẻ: " + money(subtotal) + ".";
            if (!withinBudget(subtotal, budget)) total += " Tổng các món này vượt ngân sách bạn đưa ra.";
        }
        return new Recommendation(describe(kit, "Chưa có kit còn hàng phù hợp với layout và ngân sách."),
            pcbText, describeSwitch(switches, quiet), stabilizerText, keycapText, total, List.copyOf(cards));
    }

    private Component component(Product product) {
        return new Component(product, ProductSpecifications.read(mapper, product.getSpecs()));
    }

    private static Component first(List<Component> parts, Predicate<Component> filter) {
        return parts.stream().filter(filter).findFirst().orElse(null);
    }

    private static boolean withinBudget(BigDecimal price, BigDecimal budget) {
        return budget == null || price.compareTo(budget) <= 0;
    }

    private static boolean matchesLayout(Component part, String layout) {
        return layout == null || layout.equalsIgnoreCase(part.product().getLayout());
    }

    private static boolean matchesPlatform(Component board, Component part) {
        return board == null || (!board.field("platform").isBlank() && board.field("platform").equals(part.field("platform")));
    }

    private static boolean conflicts(Component board, Component part, String field) {
        return board != null && !board.field(field).isBlank() && !part.field(field).isBlank()
            && !board.field(field).equalsIgnoreCase(part.field(field));
    }

    private static boolean matchesKeycapSpecs(Component board, Component keycap) {
        return board != null && KEYCAP_SPECS.stream().allMatch(field -> !board.field(field).isBlank()
            && board.field(field).equalsIgnoreCase(keycap.field(field)));
    }

    private static boolean supportsPins(Component board, Component part) {
        int pins = part.specs().path("pinCount").asInt(0);
        if (board == null || pins <= 0 || !board.specs().path("supportedPins").isArray()) return true;
        for (JsonNode supported : board.specs().path("supportedPins")) {
            if (supported.asInt() == pins) return true;
        }
        return false;
    }

    private static boolean included(Component kit, String label) {
        return kit != null && Stream.of(kit.field("includes").split(","))
            .anyMatch(item -> label.equalsIgnoreCase(item.strip()));
    }

    private static String describe(Component part, String unavailable) {
        return part == null ? unavailable : part.product().getName() + " — " + money(part.product().getPrice()) + ".";
    }

    private static String describeSwitch(SwitchChoice choice, boolean quiet) {
        if (choice == null) return "Chưa có switch còn hàng phù hợp với yêu cầu.";
        String result = describe(choice.part(), "");
        if (choice.quantityKnown() && choice.quantity() > 1) {
            result += " Cần " + choice.quantity() + " gói, tạm tính " + money(choice.cost()) + ".";
        } else if (!choice.quantityKnown()) {
            result += " Cần biết số phím và số switch mỗi đơn vị bán để tính đủ số lượng.";
        }
        if (quiet && !choice.part().silent()) result += " Dữ liệu shop chưa xác nhận mẫu này là silent.";
        return result;
    }

    static String money(BigDecimal price) {
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(price) + " ₫";
    }

    record Recommendation(String kit, String pcb, String switches, String stabilizer, String keycap,
                          String total, List<ChatProductResponse> products) {}

    private record Component(Product product, JsonNode specs) {
        String field(String name) { return specs.path(name).asString(""); }
        String name() { return RuleBasedChatbotService.normalize(product.getName()); }
        boolean type(String type) { return type.equalsIgnoreCase(field("componentType")); }
        boolean inCategory(String slug) { return slug.equals(product.getCategory().getSlug()); }
        boolean silent() { return specs.path("silent").asBoolean(false) || SILENT.matcher(name()).find(); }
    }

    private record SwitchChoice(Component part, int keyCount) {
        boolean quantityKnown() { return keyCount > 0 && part.specs().path("pack").asInt(0) > 0; }
        int quantity() { return quantityKnown() ? Math.ceilDiv(keyCount, part.specs().path("pack").asInt()) : 1; }
        BigDecimal cost() { return part.product().getPrice().multiply(BigDecimal.valueOf(quantity())); }
    }
}
