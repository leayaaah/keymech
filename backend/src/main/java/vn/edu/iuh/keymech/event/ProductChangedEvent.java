package vn.edu.iuh.keymech.event;

import org.springframework.context.ApplicationEvent;

/**
 * Sự kiện khi thông tin sản phẩm thay đổi (Staff tạo/sửa/ẩn/hiện).
 * Dùng để kích hoạt các hành động phụ như:
 * - Index lại product cho Chatbot RAG
 * - Xóa cache liên quan
 * - Gửi email thông báo (nếu có)
 */
public class ProductChangedEvent extends ApplicationEvent {

    private final Long productId;
    /** "created" | "updated" | "hidden" | "visible" */
    private final String changeType;

    public ProductChangedEvent(Object source, Long productId, String changeType) {
        super(source);
        this.productId = productId;
        this.changeType = changeType;
    }

    public Long getProductId() { return productId; }

    public String getChangeType() { return changeType; }
}
