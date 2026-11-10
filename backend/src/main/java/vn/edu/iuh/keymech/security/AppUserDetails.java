package vn.edu.iuh.keymech.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.iuh.keymech.model.User;

public class AppUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final boolean enabled;

    public AppUserDetails(User u) {
        this.id = u.getId(); this.email = u.getEmail(); this.password = u.getPassword();
        this.role = u.getRole().name(); this.enabled = u.isEnabled();
    }
    public Long getId() { return id; }
    public String getRole() { return role; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return enabled; }
}
