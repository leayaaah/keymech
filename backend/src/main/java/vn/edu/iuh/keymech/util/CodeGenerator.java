package vn.edu.iuh.keymech.util;

import java.security.SecureRandom;

public final class CodeGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGenerator() {}

    /**
     * Tạo mã đơn hàng dạng ORD-XXXXXXXXXX (10 ký tự chữ hoa + số).
     */
    public static String orderCode() {
        return "ORD-" + alphanumeric(10);
    }

    /**
     * Tạo mã yêu cầu Custom dạng CUS-XXXXXXXXXX (10 ký tự chữ hoa + số).
     */
    public static String customCode() {
        return "CUS-" + alphanumeric(10);
    }

    /**
     * Tạo mã xác nhận quên mật khẩu (6 chữ số).
     */
    public static String resetCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private static String alphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
