package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import vn.edu.iuh.keymech.model.Product;

public record ChatProductResponse(Long id, String name, BigDecimal price,
                                  String imageUrl, boolean inStock, String url, int quantity) {
    public static ChatProductResponse from(Product product) {
        return from(product, 1);
    }

    public static ChatProductResponse from(Product product, int quantity) {
        return new ChatProductResponse(product.getId(), product.getName(), product.getPrice(),
            product.getImageUrl(), product.getStockQuantity() >= quantity, "/products/" + product.getId(), quantity);
    }
}
