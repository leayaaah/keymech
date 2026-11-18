package vn.edu.iuh.keymech.exception;

public class InvalidCodeException extends RuntimeException {
    private final String code;
    public InvalidCodeException(String code, String message) { super(message); this.code = code; }
    public String getCode() { return code; }
}
