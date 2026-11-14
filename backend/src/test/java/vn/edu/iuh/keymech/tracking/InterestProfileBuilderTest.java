package vn.edu.iuh.keymech.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.iuh.keymech.dto.ActivitySignal;
import vn.edu.iuh.keymech.dto.InterestProfile;
import vn.edu.iuh.keymech.model.enums.EventType;

class InterestProfileBuilderTest {

    private static ActivitySignal event(EventType t, Long pid, String name, String layout, String sw,
                                        String brand, Long cat, long price, String kw) {
        return new ActivitySignal(t, pid, name, layout, sw, brand, cat,
            pid == null ? null : BigDecimal.valueOf(price), kw, LocalDateTime.now());
    }

    @Test
    @DisplayName("Không có sự kiện thì hồ sơ rỗng")
    void build_noEvents_isEmpty() {
        assertThat(InterestProfileBuilder.build(null).isEmpty()).isTrue();
        assertThat(InterestProfileBuilder.build(List.of()).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Tổng hợp danh mục, layout, switch, hãng, giá trung bình, sản phẩm và từ khóa gần nhất")
    void build_aggregatesPreferences() {
        // thứ tự: mới nhất trước
        List<ActivitySignal> signals = List.of(
            event(EventType.SEARCH, null, null, null, null, null, null, 0, "keychron k2"),
            event(EventType.VIEW_PRODUCT, 1L, "Keychron K2 75%", "75%", "Tactile", "Keychron", 1L, 2_200_000, null),
            event(EventType.ADD_TO_CART, 1L, "Keychron K2 75%", "75%", "Tactile", "Keychron", 1L, 2_200_000, null),
            event(EventType.VIEW_PRODUCT, 2L, "Akko 3068", "65%", "Linear", "Akko", 1L, 1_000_000, null),
            event(EventType.VIEW_PRODUCT, 3L, "Switch Gateron", null, "Linear", "Gateron", 2L, 200_000, null),
            event(EventType.SEARCH, null, null, null, null, null, 1L, 0, "KEYCHRON K2"),
            event(EventType.SEARCH, null, null, null, null, null, null, 0, "phím 65%"));

        InterestProfile p = InterestProfileBuilder.build(signals);

        assertThat(p.topCategoryIds()).containsExactly(1L, 2L);
        assertThat(p.topLayout()).isEqualTo("75%");
        assertThat(p.topSwitchType()).isEqualTo("Tactile");
        assertThat(p.topBrand()).isEqualTo("Keychron");
        assertThat(p.averagePrice()).isEqualByComparingTo("1133333");   // 3 sản phẩm xem phân biệt
        assertThat(p.recentProducts()).extracting(InterestProfile.RecentProduct::id).containsExactly(1L, 2L, 3L);
        assertThat(p.recentKeywords()).containsExactly("keychron k2", "phím 65%");   // lọc trùng không phân biệt hoa thường
        assertThat(p.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Hòa điểm thì thuộc tính ở sự kiện gần hơn thắng")
    void build_tieBreaksByRecency() {
        InterestProfile p = InterestProfileBuilder.build(List.of(
            event(EventType.VIEW_PRODUCT, 5L, "A", "TKL", null, null, 9L, 1, null),
            event(EventType.VIEW_PRODUCT, 6L, "B", "60%", null, null, 8L, 1, null)));

        assertThat(p.topLayout()).isEqualTo("TKL");
        assertThat(p.topCategoryIds()).containsExactly(9L, 8L);
    }

    @Test
    @DisplayName("Mua nặng hơn xem: một lần mua (3 điểm) thắng hai lần xem (2 điểm)")
    void build_purchaseOutweighsViews() {
        InterestProfile p = InterestProfileBuilder.build(List.of(
            event(EventType.VIEW_PRODUCT, 5L, "A", "TKL", null, null, 9L, 1, null),
            event(EventType.VIEW_PRODUCT, 7L, "C", "TKL", null, null, 9L, 1, null),
            event(EventType.PURCHASE, 6L, "B", "60%", null, null, 8L, 1, null)));

        assertThat(p.topLayout()).isEqualTo("60%");
        assertThat(InterestProfileBuilder.weight(EventType.PURCHASE)).isEqualTo(3);
        assertThat(InterestProfileBuilder.weight(EventType.ADD_TO_CART)).isEqualTo(2);
        assertThat(InterestProfileBuilder.weight(EventType.VIEW_PRODUCT)).isEqualTo(1);
        assertThat(InterestProfileBuilder.weight(EventType.SEARCH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Giới hạn: 2 danh mục, 3 sản phẩm gần nhất, 5 từ khóa gần nhất")
    void build_respectsLimits() {
        List<ActivitySignal> many = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            many.add(event(EventType.VIEW_PRODUCT, (long) i, "P" + i, null, null, null, (long) i, 100, null));
        }
        for (int i = 1; i <= 10; i++) {
            many.add(event(EventType.SEARCH, null, null, null, null, null, null, 0, "k" + i));
        }

        InterestProfile p = InterestProfileBuilder.build(many);

        assertThat(p.topCategoryIds()).hasSize(2);
        assertThat(p.recentProducts()).hasSize(3);
        assertThat(p.recentKeywords()).hasSize(5);
    }

    @Test
    @DisplayName("Hồ sơ không chứa dữ liệu định danh: chỉ thuộc tính sản phẩm và từ khóa")
    void profile_hasNoIdentifyingFields() {
        assertThat(InterestProfile.class.getRecordComponents())
            .extracting(c -> c.getName())
            .containsExactly("topCategoryIds", "topLayout", "topSwitchType", "topBrand",
                "averagePrice", "recentProducts", "recentKeywords");
    }
}
