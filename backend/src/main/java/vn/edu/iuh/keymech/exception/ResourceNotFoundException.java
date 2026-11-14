package vn.edu.iuh.keymech.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() { super("Không tìm thấy dữ liệu"); }
    public ResourceNotFoundException(String message) { super(message); }
}
