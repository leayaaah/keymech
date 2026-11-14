package vn.edu.iuh.keymech.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import vn.edu.iuh.keymech.repository.UserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public AppUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return users.findByEmail(email)
            .map(AppUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
    }
}
