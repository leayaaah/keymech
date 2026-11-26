package vn.edu.iuh.keymech.dto.product;

import java.math.BigDecimal;
import vn.edu.iuh.keymech.model.Product;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;

public record StaffProductResponse(
        Long id, String name, String brand, Long categoryId, String categoryName,
        String layout, String switchType, BigDecimal price, int stockQuantity,
        String description, String specs, String imageUrl, VisibilityStatus status) {

    public static StaffProductResponse from(Product product) {
        return new StaffProductResponse(product.getId(), product.getName(), product.getBrand(),
                product.getCategory().getId(), product.getCategory().getName(), product.getLayout(),
                product.getSwitchType(), product.getPrice(), product.getStockQuantity(), product.getDescription(),
                product.getSpecs(), product.getImageUrl(), product.getStatus());
    }
}
