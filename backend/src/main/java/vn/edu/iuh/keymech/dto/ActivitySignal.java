package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.edu.iuh.keymech.model.enums.EventType;

/**
 * Một sự kiện hành vi kèm thuộc tính sản phẩm liên quan (null nếu sự kiện không có sản phẩm,
 * ví dụ SEARCH). Chỉ dùng nội bộ để tổng hợp hồ sơ quan tâm; không chứa dữ liệu định danh.
 */
public record ActivitySignal(
    EventType eventType,
    Long productId,
    String productName,
    String layout,
    String switchType,
    String brand,
    Long categoryId,
    BigDecimal price,
    String keyword,
    LocalDateTime createdAt
) {}
