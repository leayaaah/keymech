package vn.edu.iuh.keymech.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Hồ sơ quan tâm của một khách, tổng hợp từ user_activity 30 ngày gần nhất.
 * Chỉ gồm thuộc tính sản phẩm và từ khóa tìm kiếm; KHÔNG có tên, email, địa chỉ, số điện thoại
 * hay mã đơn nên có thể đưa vào prompt Chatbot (quy ước mục 9).
 */
public record InterestProfile(
    List<Long> topCategoryIds,        // tối đa 2, điểm cao nhất trước
    String topLayout,                 // null nếu chưa có
    String topSwitchType,
    String topBrand,
    BigDecimal averagePrice,          // giá trung bình sản phẩm đã xem hoặc mua, null nếu chưa có
    List<RecentProduct> recentProducts, // tối đa 3 sản phẩm xem gần nhất
    List<String> recentKeywords       // tối đa 5 từ khóa tìm kiếm gần nhất
) {

    public record RecentProduct(Long id, String name) {}

    public static InterestProfile empty() {
        return new InterestProfile(List.of(), null, null, null, null, List.of(), List.of());
    }

    public boolean isEmpty() {
        return topCategoryIds.isEmpty() && topLayout == null && topSwitchType == null
            && topBrand == null && averagePrice == null
            && recentProducts.isEmpty() && recentKeywords.isEmpty();
    }
}
