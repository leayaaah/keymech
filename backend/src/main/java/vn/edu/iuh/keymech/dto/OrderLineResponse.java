package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import vn.edu.iuh.keymech.model.OrderItem;

public record OrderLineResponse(Long id, Long productId, String productName, int quantity,
                                BigDecimal unitPrice, BigDecimal lineTotal) {

    public static List<OrderLineResponse> fromItems(List<OrderItem> items) {
        return items.stream().sorted(Comparator.comparing(OrderItem::getId))
            .map(item -> new OrderLineResponse(item.getId(), item.getProduct().getId(), item.getProduct().getName(),
                item.getQuantity(), item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
            .toList();
    }
}
