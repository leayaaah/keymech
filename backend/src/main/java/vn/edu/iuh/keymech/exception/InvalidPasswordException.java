package vn.edu.iuh.keymech.exception;

public class InvalidPasswordException extends RuntimeException {
    private final String code;
    public InvalidPasswordException(String code, String message) { super(message); this.code = code; }
    public String getCode() { return code; }
}
