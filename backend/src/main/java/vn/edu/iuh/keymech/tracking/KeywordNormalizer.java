package vn.edu.iuh.keymech.tracking;

/** Chuẩn hóa từ khóa tìm kiếm trước khi ghi user_activity. Lớp thuần Java. */
final class KeywordNormalizer {

    static final int MAX_LENGTH = 100;   // khớp cột user_activity.keyword VARCHAR(100)

    private KeywordNormalizer() {}

    /** Cắt khoảng trắng đầu cuối, gộp khoảng trắng liên tiếp, cắt còn 100 ký tự; rỗng thì trả null. */
    static String normalize(String keyword) {
        if (keyword == null) return null;
        String k = keyword.trim().replaceAll("\\s+", " ");
        if (k.isEmpty()) return null;
        return k.length() > MAX_LENGTH ? k.substring(0, MAX_LENGTH) : k;
    }
}
