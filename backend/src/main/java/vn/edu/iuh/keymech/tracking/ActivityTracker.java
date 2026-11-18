package vn.edu.iuh.keymech.tracking;

import vn.edu.iuh.keymech.model.enums.EventType;

/**
 * Ghi sự kiện hành vi (user tracking) cho Chatbot cá nhân hóa.
 * Chỉ ghi Guest và Customer — Staff và Admin bị bỏ qua ở implementation.
 * Chạy bất đồng bộ (@Async), lỗi chỉ ghi log, không làm chậm request.
 *
 * Lưu ý: Phương sở hữu package này; Thuận và Thùy chỉ gọi.
 */
public interface ActivityTracker {

    /**
     * Ghi một sự kiện.
     *
     * @param type      loại sự kiện (VIEW_PRODUCT, SEARCH, ADD_TO_CART, PURCHASE)
     * @param visitorId UUID trong Session (null khi đã đăng nhập)
     * @param userId    id User (null khi là Guest)
     * @param productId id sản phẩm liên quan (null với SEARCH)
     * @param categoryId id danh mục liên quan (null nếu không có)
     * @param keyword   từ khóa tìm kiếm (chỉ với SEARCH, tối đa 100 ký tự)
     */
    void record(EventType type, String visitorId, Long userId,
                Long productId, Long categoryId, String keyword);
}