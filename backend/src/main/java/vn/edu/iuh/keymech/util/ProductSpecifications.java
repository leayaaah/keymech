package vn.edu.iuh.keymech.util;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static JsonNode read(ObjectMapper mapper, String text) {
        if (text != null) {
            try {
                JsonNode node = mapper.readTree(text);
                if (node != null && node.isObject()) return node;
            } catch (JacksonException ignored) {
                // Sản phẩm cũ có thể chỉ có mô tả tự do trong specs.
            }
        }
        return mapper.createObjectNode();
    }
}
