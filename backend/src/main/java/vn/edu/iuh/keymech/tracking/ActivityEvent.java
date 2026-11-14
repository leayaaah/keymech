package vn.edu.iuh.keymech.tracking;

import vn.edu.iuh.keymech.model.enums.EventType;

/**
 * Sự kiện đã được chuẩn hóa ở luồng gọi (đã biết visitor_id, user_id), truyền sang luồng ghi bất đồng bộ.
 * Chỉ gồm kiểu nguyên thủy, không mang HttpSession hay entity sang luồng khác (quy ước mục 6.5).
 */
public record ActivityEvent(String visitorId, Long userId, EventType eventType,
                     Long productId, Long categoryId, String keyword) {}
