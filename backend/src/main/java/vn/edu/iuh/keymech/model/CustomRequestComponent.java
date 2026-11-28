package vn.edu.iuh.keymech.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;

@Embeddable
public record CustomRequestComponent(
        @Enumerated(EnumType.STRING) @Column(name = "component_slot", nullable = false, length = 16) Slot slot,
        @Column(name = "product_id", nullable = false) Long productId,
        @Column(name = "product_name", nullable = false, length = 200) String name,
        @Column(nullable = false) int quantity,
        @Column(name = "unit_price", nullable = false, precision = 12, scale = 0) BigDecimal unitPrice) {

    public enum Slot { KIT, SWITCH, KEYCAP, STABILIZER }

    public static CustomRequestComponent of(Slot slot, Product product, int quantity) {
        return new CustomRequestComponent(slot, product.getId(), product.getName(), quantity, product.getPrice());
    }
}
