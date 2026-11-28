package vn.edu.iuh.keymech.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import vn.edu.iuh.keymech.dto.CustomKitOption;
import vn.edu.iuh.keymech.dto.CustomRequestInput;
import vn.edu.iuh.keymech.exception.BusinessException;
import vn.edu.iuh.keymech.model.CustomRequestComponent;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.repository.ProductRepository;
import vn.edu.iuh.keymech.util.ProductSpecifications;

import static vn.edu.iuh.keymech.model.CustomRequestComponent.Slot.*;
import static vn.edu.iuh.keymech.model.enums.VisibilityStatus.ACTIVE;

@Service
@RequiredArgsConstructor
public class CustomCatalogService {
    private static final List<String> KEYCAP_SPECS = List.of("stem", "physicalLayout", "spacebarWidth", "rightShiftWidth");
    private static final Set<String> SWITCH_TYPES = Set.of("Linear", "Tactile", "Clicky");
    private static final Set<String> LAYOUTS = Set.of("60%", "65%", "75%", "TKL", "Fullsize");
    private static final Set<String> REQUIRED_KIT_PARTS = Set.of("Case", "Plate", "PCB");

    private final ProductRepository products;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public List<CustomKitOption> options() {
        List<Part> catalog = products.findInStockWithVisibleCategory(ACTIVE).stream().map(this::part).toList();
        return catalog.stream().filter(Part::supportedKit).map(kit -> optionsFor(kit, catalog))
            .filter(option -> !option.switches().isEmpty() && !option.keycaps().isEmpty()
                && (option.stabilizersIncluded() || !option.stabilizers().isEmpty()))
            .toList();
    }

    // Called inside the create transaction so availability and configuration are checked together.
    Selection select(CustomRequestInput input) {
        List<Long> ids = Stream.of(input.kitProductId(), input.switchProductId(), input.keycapProductId(),
                input.stabilizerProductId()).filter(id -> id != null).sorted().toList();
        if (ids.stream().distinct().count() != ids.size()) {
            throw invalid("Mỗi sản phẩm chỉ được chọn cho một vị trí trong cấu hình.");
        }
        Map<Long, Part> selected = ids.stream().map(this::loadVisibleForCreate)
            .collect(Collectors.toMap(part -> part.product().getId(), Function.identity()));
        Part kit = selected.get(input.kitProductId());
        Part switches = selected.get(input.switchProductId());
        Part keycaps = selected.get(input.keycapProductId());
        Part stabilizer = selected.get(input.stabilizerProductId());

        if (!kit.supportedKit()) throw incompatible("Kit phải có vỏ, plate, PCB và đủ thông số tương thích.");
        if (!switches.matchesSwitch(kit)) throw incompatible("Switch không đúng chuẩn, số chân hoặc stem mà kit hỗ trợ.");
        if (!keycaps.matchesKeycaps(kit)) throw incompatible("Keycap không khớp stem, bố trí phím hoặc kích thước phím của kit.");
        if (kit.includesStabilizers() && stabilizer != null) throw invalid("Kit đã kèm stabilizer.");
        if (!kit.includesStabilizers()) {
            if (stabilizer == null) throw invalid("Vui lòng chọn stabilizer cho kit này.");
            if (!stabilizer.matchesStabilizer(kit)) throw incompatible("Stabilizer không đúng kiểu gắn hoặc kích thước cho kit.");
        }

        int switchQuantity = switches.switchQuantity(kit);
        List<CustomRequestComponent> components = new ArrayList<>(List.of(
            snapshot(KIT, kit, 1), snapshot(SWITCH, switches, switchQuantity), snapshot(KEYCAP, keycaps, 1)));
        if (stabilizer != null) components.add(snapshot(STABILIZER, stabilizer, 1));
        return new Selection(kit.product().getLayout(), switches.product().getSwitchType(), switches.product().getName(),
            keycaps.product().getName(), kit.field("case"), kit.specs().path("hotSwap").asBoolean(), List.copyOf(components));
    }

    private CustomKitOption optionsFor(Part kit, List<Part> catalog) {
        List<CustomKitOption.Part> switches = catalog.stream().filter(part -> part.matchesSwitch(kit))
            .filter(part -> part.product().getStockQuantity() >= part.switchQuantity(kit))
            .map(part -> CustomKitOption.Part.of(part.product(), part.switchQuantity(kit))).toList();
        List<CustomKitOption.Part> keycaps = catalog.stream().filter(part -> part.matchesKeycaps(kit))
            .map(part -> CustomKitOption.Part.of(part.product(), 1)).toList();
        List<CustomKitOption.Part> stabilizers = kit.includesStabilizers() ? List.of() : catalog.stream()
            .filter(part -> part.matchesStabilizer(kit)).map(part -> CustomKitOption.Part.of(part.product(), 1)).toList();
        return new CustomKitOption(CustomKitOption.Part.of(kit.product(), 1), kit.product().getLayout(),
            kit.specs().path("hotSwap").asBoolean(), kit.includesStabilizers(), switches, keycaps, stabilizers);
    }

    private Part loadVisibleForCreate(long id) {
        Product product = products.lockById(id).filter(p -> p.getStatus() == ACTIVE && p.getCategory().getStatus() == ACTIVE)
            .orElseThrow(() -> new BusinessException("CUSTOM_COMPONENT_UNAVAILABLE", "Linh kiện đã chọn hiện không còn được bán."));
        return part(product);
    }

    private static CustomRequestComponent snapshot(CustomRequestComponent.Slot slot, Part part, int quantity) {
        if (part.product().getStockQuantity() < quantity) {
            throw new BusinessException("OUT_OF_STOCK", part.product().getName() + " không còn đủ số lượng. Vui lòng cập nhật cấu hình.");
        }
        return CustomRequestComponent.of(slot, part.product(), quantity);
    }

    private Part part(Product product) {
        return new Part(product, ProductSpecifications.read(mapper, product.getSpecs()));
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    private static BusinessException incompatible(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, "CUSTOM_COMPONENT_INCOMPATIBLE", message);
    }

    record Selection(String layout, String switchType, String switchName, String keycapName, String caseMaterial,
                     boolean hotSwap, List<CustomRequestComponent> components) {}

    private record Part(Product product, JsonNode specs) {
        String field(String name) { return specs.path(name).asString(""); }
        boolean category(String slug) { return slug.equals(product.getCategory().getSlug()); }
        boolean type(String type) { return type.equalsIgnoreCase(field("componentType")); }
        boolean includes(String label) {
            return Stream.of(field("includes").split(",")).anyMatch(value -> label.equalsIgnoreCase(value.strip()));
        }
        boolean same(Part other, String name) {
            return !field(name).isBlank() && field(name).equalsIgnoreCase(other.field(name));
        }

        boolean supportedKit() {
            return category("kit-ban-phim") && product.getLayout() != null && LAYOUTS.contains(product.getLayout())
                && REQUIRED_KIT_PARTS.stream().allMatch(this::includes) && specs.path("keys").asInt(0) > 0
                && specs.path("hotSwap").isBoolean() && !field("switchFamily").isBlank()
                && specs.path("supportedPins").isArray() && specs.path("supportedPins").size() > 0
                && KEYCAP_SPECS.stream().allMatch(field -> !field(field).isBlank())
                && (includesStabilizers() || (!field("platform").isBlank() && !field("stabilizerMount").isBlank()
                    && !sizes("stabilizerSizes").isEmpty()));
        }

        boolean includesStabilizers() { return includes("stabilizer") || includes("stabilizers"); }

        boolean matchesSwitch(Part kit) {
            int pins = specs.path("pinCount").asInt(0);
            String switchType = product.getSwitchType();
            return category("switch") && switchType != null && SWITCH_TYPES.contains(switchType)
                && same(kit, "switchFamily") && same(kit, "stem") && specs.path("pack").asInt(0) > 0 && pins > 0
                && kit.specs().path("supportedPins").valueStream().anyMatch(value -> value.asInt(0) == pins);
        }

        int switchQuantity(Part kit) {
            return Math.ceilDiv(kit.specs().path("keys").asInt(), specs.path("pack").asInt());
        }

        boolean matchesKeycaps(Part kit) {
            return category("keycap") && type("keycap-set")
                && supportsLayout(kit.product().getLayout())
                && KEYCAP_SPECS.stream().allMatch(field -> same(kit, field));
        }

        private boolean supportsLayout(String layout) {
            return layout.equals(product.getLayout()) || specs.path("supportedLayouts").valueStream()
                .anyMatch(value -> layout.equals(value.asString("")));
        }

        boolean matchesStabilizer(Part kit) {
            return category("dung-cu-phu-kien") && type("stabilizer") && same(kit, "platform")
                && field("mount").equalsIgnoreCase(kit.field("stabilizerMount"))
                && sizes("sizes").equals(kit.sizes("stabilizerSizes"));
        }

        List<String> sizes(String key) {
            var values = specs.path(key);
            return values.isArray() ? values.valueStream().map(value -> value.asString("")).sorted().toList() : List.of();
        }
    }
}
