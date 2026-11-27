package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import vn.edu.iuh.keymech.model.Product;

public record ChatProductResponse(Long id, String name, BigDecimal price,
                                  String imageUrl, boolean inStock, String url) {
    public static ChatProductResponse from(Product product) {
        return new ChatProductResponse(product.getId(), product.getName(), product.getPrice(),
            product.getImageUrl(), product.getStockQuantity() > 0, "/products/" + product.getId());
    }
}
