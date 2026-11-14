package vn.edu.iuh.keymech.tracking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import vn.edu.iuh.keymech.dto.ActivitySignal;
import vn.edu.iuh.keymech.dto.InterestProfile;
import vn.edu.iuh.keymech.dto.InterestProfile.RecentProduct;
import vn.edu.iuh.keymech.model.enums.EventType;

/**
 * Dựng {@link InterestProfile} từ danh sách sự kiện (mới nhất trước). Lớp thuần Java, không phụ thuộc
 * Spring hay CSDL nên kiểm thử trực tiếp được.
 *
 * <p>Trọng số: PURCHASE 3, ADD_TO_CART 2, VIEW_PRODUCT 1, SEARCH 1. Điểm bằng nhau thì thuộc tính
 * xuất hiện ở sự kiện gần hơn được ưu tiên.
 */
final class InterestProfileBuilder {

    static final int MAX_TOP_CATEGORIES = 2;
    static final int MAX_RECENT_PRODUCTS = 3;
    static final int MAX_RECENT_KEYWORDS = 5;

    private InterestProfileBuilder() {}

    static InterestProfile build(List<ActivitySignal> signals) {
        if (signals == null || signals.isEmpty()) {
            return InterestProfile.empty();
        }
        Map<Long, Integer> categoryScore = new LinkedHashMap<>();
        Map<String, Integer> layoutScore = new LinkedHashMap<>();
        Map<String, Integer> switchScore = new LinkedHashMap<>();
        Map<String, Integer> brandScore = new LinkedHashMap<>();
        Map<Long, BigDecimal> pricedProducts = new LinkedHashMap<>();   // mỗi sản phẩm tính một lần
        Map<Long, String> recentProducts = new LinkedHashMap<>();
        Set<String> keywordsSeen = new LinkedHashSet<>();
        List<String> recentKeywords = new ArrayList<>();

        for (ActivitySignal s : signals) {
            int w = weight(s.eventType());
            add(categoryScore, s.categoryId(), w);
            add(layoutScore, blankToNull(s.layout()), w);
            add(switchScore, blankToNull(s.switchType()), w);
            add(brandScore, blankToNull(s.brand()), w);

            if ((s.eventType() == EventType.VIEW_PRODUCT || s.eventType() == EventType.PURCHASE)
                && s.productId() != null && s.price() != null) {
                pricedProducts.putIfAbsent(s.productId(), s.price());
            }
            if (s.eventType() == EventType.VIEW_PRODUCT && s.productId() != null
                && s.productName() != null && recentProducts.size() < MAX_RECENT_PRODUCTS) {
                recentProducts.putIfAbsent(s.productId(), s.productName());
            }
            if (s.eventType() == EventType.SEARCH && s.keyword() != null
                && recentKeywords.size() < MAX_RECENT_KEYWORDS
                && keywordsSeen.add(s.keyword().toLowerCase())) {
                recentKeywords.add(s.keyword());
            }
        }

        List<Long> topCategories = topKeys(categoryScore, MAX_TOP_CATEGORIES);
        BigDecimal avg = pricedProducts.isEmpty() ? null
            : pricedProducts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(pricedProducts.size()), 0, RoundingMode.HALF_UP);

        return new InterestProfile(
            topCategories,
            first(topKeys(layoutScore, 1)),
            first(topKeys(switchScore, 1)),
            first(topKeys(brandScore, 1)),
            avg,
            recentProducts.entrySet().stream().map(e -> new RecentProduct(e.getKey(), e.getValue())).toList(),
            List.copyOf(recentKeywords));
    }

    static int weight(EventType type) {
        return switch (type) {
            case PURCHASE -> 3;
            case ADD_TO_CART -> 2;
            case VIEW_PRODUCT, SEARCH -> 1;
        };
    }

    private static <K> void add(Map<K, Integer> scores, K key, int w) {
        if (key != null) scores.merge(key, w, Integer::sum);
    }

    /** Khóa điểm cao nhất; sort ổn định trên LinkedHashMap (thứ tự chèn = mới nhất trước) nên điểm bằng nhau thì khóa gần hơn thắng. */
    private static <K> List<K> topKeys(Map<K, Integer> scores, int limit) {
        return scores.entrySet().stream()
            .sorted(Map.Entry.<K, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .toList();
    }

    private static <T> T first(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
