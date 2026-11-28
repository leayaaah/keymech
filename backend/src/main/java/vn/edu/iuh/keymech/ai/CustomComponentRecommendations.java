package vn.edu.iuh.keymech.ai;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import vn.edu.iuh.keymech.dto.ChatContext;
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

    Recommendation recommend(List<Product> catalog, ChatContext context) {
        var available = catalog.stream().filter(product -> product.getStockQuantity() > 0)
            .map(this::component)
            .sorted(Comparator.comparing((Component part) -> part.product().getPrice())
                .thenComparing(part -> part.product().getId()))
            .toList();

        var build = available.stream()
            .filter(part -> part.inCategory("kit-ban-phim") && matchesLayout(part, context.layout()))
            .map(kit -> build(available, kit, context))
            .min(Comparator.comparing(Build::complete).reversed()
                .thenComparing(candidate -> !withinBudget(candidate.subtotal(), context.budget()))
                .thenComparing(candidate -> withinBudget(candidate.subtotal(), context.budget())
                    && Boolean.TRUE.equals(context.quiet()) && !candidate.quiet())
                .thenComparing(Build::subtotal))
            .orElseGet(() -> build(available, null, context));
        return describeBuild(build, available, context);
    }

    private Build build(List<Component> available, Component kit, ChatContext context) {
        String layout = context.layout();
        boolean quiet = Boolean.TRUE.equals(context.quiet());
        String requestedSwitch = context.switchName();
        boolean bundledPcb = included(kit, "PCB");
        Component pcb = bundledPcb || kit == null ? null : first(available, part -> part.type("pcb")
            && matchesLayout(part, layout) && matchesPlatform(kit, part));
        Component board = pcb == null ? kit : pcb;
        boolean bundledStabilizer = included(kit, "stabilizer");
        Component stabilizer = bundledStabilizer || board == null ? null : first(available, part -> part.type("stabilizer")
            && matchesPlatform(board, part) && (matchesLayout(part, layout) || part.product().getLayout() == null));
        Component keycap = available.stream().filter(part -> part.inCategory("keycap"))
            .filter(part -> part.type("keycap-set") || KEYCAP_SET.matcher(part.name()).find())
            .filter(part -> matchesLayout(part, layout) || part.product().getLayout() == null)
            .filter(part -> KEYCAP_SPECS.stream().noneMatch(field -> conflicts(board, part, field)))
            .min(Comparator.comparing(part -> !matchesKeycapSpecs(board, part))).orElse(null);

        int keys = board == null ? 0 : board.specs().path("keys").asInt(0);
        BigDecimal base = Stream.of(kit, pcb, stabilizer, keycap).filter(Objects::nonNull)
            .map(part -> part.product().getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
        SwitchChoice switches = available.stream().filter(part -> part.inCategory("switch"))
            .filter(part -> requestedSwitch == null || RuleBasedChatbotService.matchesSwitch(part.product().getName(), requestedSwitch))
            .filter(part -> requestedSwitch != null || !quiet || part.silent() || "Linear".equalsIgnoreCase(part.product().getSwitchType())
                || "Tactile".equalsIgnoreCase(part.product().getSwitchType()))
            .filter(part -> !conflicts(board, part, "switchFamily") && supportsPins(board, part))
            .map(part -> new SwitchChoice(part, keys))
            .filter(choice -> choice.quantity() <= choice.part().product().getStockQuantity())
            .min(Comparator.comparing(SwitchChoice::quantityKnown).reversed()
                .thenComparing(choice -> !withinBudget(base.add(choice.cost()), context.budget()))
                .thenComparing(choice -> withinBudget(base.add(choice.cost()), context.budget())
                    && quiet && !choice.part().silent())
                .thenComparing(SwitchChoice::cost)).orElse(null);
        return new Build(kit, pcb, stabilizer, keycap, switches, bundledPcb, bundledStabilizer,
            base.add(switches == null ? BigDecimal.ZERO : switches.cost()));
    }

    private static Recommendation describeBuild(Build build, List<Component> available, ChatContext context) {
        var kit = build.kit();
        var pcb = build.pcb();
        var stabilizer = build.stabilizer();
        var keycap = build.keycap();
        var switches = build.switches();
        var board = pcb == null ? kit : pcb;
        BigDecimal budget = context.budget();
        boolean quiet = Boolean.TRUE.equals(context.quiet());
        boolean overBudget = !withinBudget(build.subtotal(), budget);
        String pcbText = build.bundledPcb() ? "PCB đi kèm kit " + kit.product().getName() + "." :
            describe(pcb, "Chưa có PCB rời phù hợp trong danh mục đang bán.");
        String stabilizerText = build.bundledStabilizer() ? "Stabilizer đi kèm kit " + kit.product().getName() + "." :
            describe(stabilizer, "Chưa xác nhận được stabilizer đi kèm hoặc bộ rời tương thích.");
        String keycapText = describe(keycap, "Chưa có bộ keycap phù hợp trong danh mục đang bán.")
            + (keycap != null && !matchesKeycapSpecs(board, keycap) ? " Cần đối chiếu stem và bố trí phím thực tế." : "");

        List<ChatProductResponse> cards = new ArrayList<>();
        String total = build.subtotal().signum() == 0 ? "" : "Tạm tính các món tìm được: " + money(build.subtotal()) + ". ";
        if (overBudget) {
            total += "Cấu hình gợi ý vượt " + (build.complete() ? "" : "ít nhất ")
                + money(build.subtotal().subtract(budget)) + " so với ngân sách.";
            var alternatives = available.stream()
                .filter(part -> part.inCategory("ban-phim-co") && matchesLayout(part, context.layout()))
                .filter(part -> withinBudget(part.product().getPrice(), budget))
                .sorted(Comparator.comparing((Component part) -> quiet && !part.silent())
                    .thenComparing(part -> !part.specs().path("hotSwap").asBoolean(false)))
                .limit(3).toList();
            alternatives.stream().map(part -> ChatProductResponse.from(part.product())).forEach(cards::add);
            if (!cards.isEmpty()) {
                total += " Bạn có thể xem bàn phím lắp sẵn cùng layout trong ngân sách:";
                for (var part : alternatives) {
                    total += "\n- " + part.product().getName() + " — " + readyMadeSpecs(part) + ".";
                }
            } else total += " Shop chưa có bàn phím lắp sẵn cùng layout trong ngân sách này.";
        } else {
            for (Component part : Stream.of(kit, pcb, switches == null ? null : switches.part(), stabilizer, keycap)
                    .filter(Objects::nonNull).toList()) {
                int quantity = switches != null && part == switches.part() ? switches.quantity() : 1;
                cards.add(ChatProductResponse.from(part.product(), quantity));
            }
            if (budget == null) total += "Ngân sách bạn dự kiến là bao nhiêu?";
            else if (build.complete()) total += "Linh kiện nằm trong ngân sách, chưa tính phí lắp.";
            else total += "Chưa thể xác nhận đủ cấu hình trong ngân sách vì còn thiếu linh kiện hoặc thông số tương thích.";
        }
        return new Recommendation(describe(kit, "Chưa có kit còn hàng phù hợp với layout."),
            pcbText, describeSwitch(switches, quiet, context.switchName()), stabilizerText, keycapText, total,
            List.copyOf(cards), overBudget);
    }

    private static String readyMadeSpecs(Component part) {
        var hotSwap = part.specs().path("hotSwap");
        String socket = hotSwap.isBoolean() ? (hotSwap.asBoolean() ? "có hot-swap" : "không hot-swap")
            : "chưa xác nhận hot-swap";
        return socket + ", " + part.silentLabel();
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

    private static String describeSwitch(SwitchChoice choice, boolean quiet, String requested) {
        if (choice == null) return requested == null ? "Chưa có switch còn hàng phù hợp với yêu cầu."
            : "Chưa có switch " + requested.toUpperCase(Locale.ROOT) + " đủ hàng và phù hợp với cấu hình.";
        String result = describe(choice.part(), "");
        if (choice.quantityKnown() && choice.quantity() > 1) {
            result += " Cần " + choice.quantity() + " gói, tạm tính " + money(choice.cost()) + ".";
        } else if (!choice.quantityKnown()) {
            result += " Cần biết số phím và số switch mỗi đơn vị bán để tính đủ số lượng.";
        }
        if (quiet && !choice.part().silent()) result += " " + choice.part().silentLabel() + ".";
        return result;
    }

    static String money(BigDecimal price) {
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(price) + " ₫";
    }

    record Recommendation(String kit, String pcb, String switches, String stabilizer, String keycap,
                          String total, List<ChatProductResponse> products, boolean overBudget) {}

    private record Build(Component kit, Component pcb, Component stabilizer, Component keycap, SwitchChoice switches,
                         boolean bundledPcb, boolean bundledStabilizer, BigDecimal subtotal) {
        boolean complete() {
            return kit != null && (bundledPcb || pcb != null) && (bundledStabilizer || stabilizer != null)
                && switches != null && switches.quantityKnown() && keycap != null
                && matchesKeycapSpecs(pcb == null ? kit : pcb, keycap);
        }

        boolean quiet() { return switches != null && switches.part().silent(); }
    }

    private record Component(Product product, JsonNode specs) {
        String field(String name) { return specs.path(name).asString(""); }
        String name() { return RuleBasedChatbotService.normalize(product.getName()); }
        boolean type(String type) { return type.equalsIgnoreCase(field("componentType")); }
        boolean inCategory(String slug) { return slug.equals(product.getCategory().getSlug()); }
        boolean silent() {
            var flag = specs.path("silent");
            return flag.isBoolean() ? flag.asBoolean() : SILENT.matcher(name()).find();
        }
        String silentLabel() {
            if (silent()) return "có switch silent";
            return specs.path("silent").isBoolean() ? "mẫu này không phải silent" : "dữ liệu shop chưa xác nhận mẫu này là silent";
        }
    }

    private record SwitchChoice(Component part, int keyCount) {
        boolean quantityKnown() { return keyCount > 0 && part.specs().path("pack").asInt(0) > 0; }
        int quantity() { return quantityKnown() ? Math.ceilDiv(keyCount, part.specs().path("pack").asInt()) : 1; }
        BigDecimal cost() { return part.product().getPrice().multiply(BigDecimal.valueOf(quantity())); }
    }
}
