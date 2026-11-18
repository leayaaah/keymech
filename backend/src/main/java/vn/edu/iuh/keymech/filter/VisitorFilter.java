package vn.edu.iuh.keymech.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import vn.edu.iuh.keymech.util.SessionKeys;

/**
 * Đảm bảo mỗi Guest có VISITOR_ID lưu trong Session.
 * Khi đã đăng nhập (có Authentication) thì không cần visitorId.
 * visitorId dùng để ghi UserActivity cho Chatbot cá nhân hóa.
 */
@Component
public class VisitorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getSession(false) != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                // Guest: tạo visitorId nếu chưa có
                if (request.getSession(false).getAttribute(SessionKeys.VISITOR_ID) == null) {
                    request.getSession(false).setAttribute(SessionKeys.VISITOR_ID, UUID.randomUUID().toString());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}