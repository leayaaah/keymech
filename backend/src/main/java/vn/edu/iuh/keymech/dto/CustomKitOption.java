package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import java.util.List;
import vn.edu.iuh.keymech.model.Product;

public record CustomKitOption(Part kit, String layout, boolean hotSwap, boolean stabilizersIncluded,
                              List<Part> switches, List<Part> keycaps, List<Part> stabilizers) {

    public record Part(long productId, String name, BigDecimal unitPrice, int quantity) {
        public static Part of(Product product, int quantity) {
            return new Part(product.getId(), product.getName(), product.getPrice(), quantity);
        }
    }
}
