package vn.edu.iuh.keymech.exception;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> build(HttpStatus s, String error, String msg) {
        return ResponseEntity.status(s).body(ErrorResponse.of(s.value(), error, msg));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> business(BusinessException e) {
        return build(HttpStatus.CONFLICT, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noStaticResource(NoResourceFoundException e) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> denied(AccessDeniedException e) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này");
    }

    @ExceptionHandler(RateLimitedException.class)
    public ResponseEntity<ErrorResponse> rate(RateLimitedException e) {
        return build(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getDefaultMessage()).collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> other(Exception e) {
        log.error("Lỗi không mong đợi", e);   // không trả stack trace ra ngoài
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Hệ thống đang gặp lỗi, vui lòng thử lại sau");
    }
}
