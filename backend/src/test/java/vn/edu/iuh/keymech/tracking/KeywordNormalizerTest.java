package vn.edu.iuh.keymech.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KeywordNormalizerTest {

    @Test
    @DisplayName("null hoặc toàn khoảng trắng thì trả null")
    void normalize_blank_returnsNull() {
        assertThat(KeywordNormalizer.normalize(null)).isNull();
        assertThat(KeywordNormalizer.normalize("   \t \n")).isNull();
    }

    @Test
    @DisplayName("Cắt đầu cuối và gộp khoảng trắng liên tiếp")
    void normalize_collapsesWhitespace() {
        assertThat(KeywordNormalizer.normalize("  phím   75%  \n gõ êm ")).isEqualTo("phím 75% gõ êm");
    }

    @Test
    @DisplayName("Cắt còn 100 ký tự để khớp cột VARCHAR(100)")
    void normalize_truncatesTo100() {
        assertThat(KeywordNormalizer.normalize("x".repeat(250))).hasSize(100);
    }
}
