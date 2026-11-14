package vn.edu.iuh.keymech.security;

import java.util.function.Supplier;

<<<<<<< HEAD
import org.jspecify.annotations.Nullable;
=======
>>>>>>> origin/main
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class CartAccessManager implements AuthorizationManager<RequestAuthorizationContext> {

<<<<<<< HEAD
//    @Override
    public AuthorizationDecision check(Supplier<Authentication> auth, RequestAuthorizationContext ctx) {
=======
    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> auth, RequestAuthorizationContext ctx) {
>>>>>>> origin/main
        Authentication a = auth.get();
        boolean blocked = a != null && a.getAuthorities().stream()
            .anyMatch(g -> g.getAuthority().equals("ROLE_STAFF") || g.getAuthority().equals("ROLE_ADMIN"));
        return new AuthorizationDecision(!blocked);
    }
<<<<<<< HEAD

    @Override
    public void verify(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext object) {
        return null;
    }
=======
>>>>>>> origin/main
}
