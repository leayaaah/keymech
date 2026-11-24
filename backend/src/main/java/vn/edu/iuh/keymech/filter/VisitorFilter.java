package vn.edu.iuh.keymech.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.keymech.security.AppUserDetails;
import vn.edu.iuh.keymech.util.SessionKeys;

/** Keeps a session visitor id for Guest and Customer tracking. */
public class VisitorFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth == null ? null : auth.getPrincipal();
        boolean customer = principal instanceof AppUserDetails details
            && "CUSTOMER".equals(details.getRole());
        boolean guest = auth == null || !auth.isAuthenticated()
            || "anonymousUser".equals(principal);
        if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")
                && (guest || customer)) {
            var session = request.getSession(true);
            if (session.getAttribute(SessionKeys.VISITOR_ID) == null) {
                session.setAttribute(SessionKeys.VISITOR_ID, UUID.randomUUID().toString());
            }
        }
        filterChain.doFilter(request, response);
    }
}
