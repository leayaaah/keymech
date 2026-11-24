package vn.edu.iuh.keymech.tracking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import vn.edu.iuh.keymech.config.AppProperties;
import vn.edu.iuh.keymech.model.enums.EventType;
import vn.edu.iuh.keymech.repository.UserActivityRepository;
import vn.edu.iuh.keymech.repository.UserActivityRepository.ActivityProfileRow;

/** Builds a privacy-safe, 30-day preference summary for chatbot personalization. */
@Service
public class ProfileService {
    private static final int LOOKBACK_DAYS = 30;
    private final UserActivityRepository activityRepository;
    private final AppProperties properties;

    public ProfileService(UserActivityRepository activityRepository, AppProperties properties) {
        this.activityRepository = activityRepository;
        this.properties = properties;
    }

    public InterestProfile getProfile(Long userId, String visitorId) {
        if (!properties.chatbot().personalization()
                || (userId == null && (visitorId == null || visitorId.isBlank()))) {
            return InterestProfile.empty();
        }
        List<ActivityProfileRow> rows = activityRepository.findProfileRows(
            userId, userId == null ? visitorId : null, LocalDateTime.now().minusDays(LOOKBACK_DAYS));
        return summarize(rows);
    }

    private InterestProfile summarize(List<ActivityProfileRow> rows) {
        Map<String, Double> categoryScores = weighted(rows, ActivityProfileRow::getCategoryName);
        Map<String, Double> layoutScores = weighted(rows, ActivityProfileRow::getLayout);
        Map<String, Double> switchScores = weighted(rows, ActivityProfileRow::getSwitchType);
        Map<String, Double> brandScores = weighted(rows, ActivityProfileRow::getBrand);

        List<BigDecimal> prices = rows.stream()
            .filter(row -> row.getPrice() != null)
            .filter(row -> row.getEventType() == EventType.VIEW_PRODUCT
                || row.getEventType() == EventType.PURCHASE)
            .map(ActivityProfileRow::getPrice).toList();
        BigDecimal averagePrice = prices.isEmpty() ? null
            : prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 0, java.math.RoundingMode.HALF_UP);

        List<String> recentProducts = rows.stream()
            .filter(row -> row.getEventType() == EventType.VIEW_PRODUCT)
            .map(ActivityProfileRow::getProductName).filter(ProfileService::hasText)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                set -> set.stream().limit(3).toList()));
        List<String> recentKeywords = rows.stream()
            .filter(row -> row.getEventType() == EventType.SEARCH)
            .map(ActivityProfileRow::getKeyword).filter(ProfileService::hasText)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                set -> set.stream().limit(3).toList()));

        return new InterestProfile(top(categoryScores, 2), top(layoutScores, 1),
            top(switchScores, 1), top(brandScores, 1), averagePrice, recentProducts, recentKeywords);
    }

    private Map<String, Double> weighted(List<ActivityProfileRow> rows,
                                         Function<ActivityProfileRow, String> property) {
        return rows.stream().filter(row -> hasText(property.apply(row)))
            .collect(Collectors.groupingBy(row -> property.apply(row),
                Collectors.summingDouble(row -> weight(row.getEventType()))));
    }

    private static double weight(EventType type) {
        return switch (type) {
            case PURCHASE -> 3;
            case ADD_TO_CART -> 2;
            case VIEW_PRODUCT, SEARCH -> 1;
        };
    }

    private static List<String> top(Map<String, Double> scores, int limit) {
        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER)))
            .limit(limit).map(Map.Entry::getKey).toList();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record InterestProfile(List<String> categories, List<String> layouts,
                                  List<String> switchTypes, List<String> brands,
                                  BigDecimal averageViewedOrPurchasedPrice,
                                  List<String> recentlyViewedProducts, List<String> recentSearchKeywords) {
        public InterestProfile {
            categories = List.copyOf(categories);
            layouts = List.copyOf(layouts);
            switchTypes = List.copyOf(switchTypes);
            brands = List.copyOf(brands);
            recentlyViewedProducts = List.copyOf(recentlyViewedProducts);
            recentSearchKeywords = List.copyOf(recentSearchKeywords);
        }

        public static InterestProfile empty() {
            return new InterestProfile(List.of(), List.of(), List.of(), List.of(), null, List.of(), List.of());
        }

        /** Text-only context with no user identifiers or account data. */
        public String toPromptContext() {
            List<String> context = new ArrayList<>();
            add(context, "Danh mục quan tâm", categories);
            add(context, "Layout", layouts);
            add(context, "Loại switch", switchTypes);
            add(context, "Thương hiệu", brands);
            if (averageViewedOrPurchasedPrice != null) {
                context.add("Mức giá trung bình sản phẩm đã xem/mua: " + averageViewedOrPurchasedPrice + " VND");
            }
            add(context, "Sản phẩm vừa xem", recentlyViewedProducts);
            add(context, "Từ khóa tìm gần đây", recentSearchKeywords);
            return String.join("\n", context);
        }

        private static void add(List<String> lines, String label, List<String> values) {
            if (!values.isEmpty()) lines.add(label + ": " + String.join(", ", values));
        }
    }
}
