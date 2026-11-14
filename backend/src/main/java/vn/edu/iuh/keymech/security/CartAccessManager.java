package vn.edu.iuh.keymech.security;

import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class CartAccessManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> auth, RequestAuthorizationContext ctx) {
        Authentication a = auth.get();
        boolean blocked = a != null && a.getAuthorities().stream()
            .anyMatch(g -> g.getAuthority().equals("ROLE_STAFF") || g.getAuthority().equals("ROLE_ADMIN"));
        return new AuthorizationDecision(!blocked);
    }
}
