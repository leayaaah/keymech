package vn.edu.iuh.keymech.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.edu.iuh.keymech.security.CartAccessManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(props.corsOrigin()));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/api/**", cfg);
        return src;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    CartAccessManager cartAccess,
                                    SecurityContextRepository repo,
                                    @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        http
          .csrf(AbstractHttpConfigurer::disable)       // giai đoạn dev; quyết định bật lại ở Sprint 4
          .cors(c -> c.configurationSource(cors))
          .securityContext(sc -> sc.securityContextRepository(repo))
          .authorizeHttpRequests(a -> a
            // Matcher cụ thể đặt trước matcher chung
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
            .requestMatchers("/api/orders/**", "/api/custom-requests/**", "/api/users/me/**").hasRole("CUSTOMER")
            .requestMatchers("/api/cart/**").access(cartAccess)
            .requestMatchers("/api/auth/**", "/api/products/**", "/api/categories/**",
                             "/api/chat/**", "/uploads/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**").permitAll()
            .anyRequest().denyAll())
          .exceptionHandling(e -> e
            .authenticationEntryPoint((req, res, ex) ->
                writeError(res, 401, "UNAUTHORIZED", "Vui lòng đăng nhập"))
            .accessDeniedHandler((req, res, ex) ->
                writeError(res, 403, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này")))
          .formLogin(AbstractHttpConfigurer::disable)
          .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    private static void writeError(HttpServletResponse res, int status, String error, String msg)
        throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(
            "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}"
                .formatted(status, error, msg, LocalDateTime.now()));
    }
}
