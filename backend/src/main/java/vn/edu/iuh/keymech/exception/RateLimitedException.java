package vn.edu.iuh.keymech.exception;

public class RateLimitedException extends RuntimeException {
    public RateLimitedException() { super("Bạn gửi câu hỏi quá nhanh, vui lòng thử lại sau"); }
}
