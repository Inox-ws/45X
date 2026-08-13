package com.inox.x45.config;

import com.inox.x45.security.EntraJwtRoleConverter;
import com.inox.x45.security.LocalJwtAuthenticationFilter;
import com.inox.x45.security.LocalJwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Deny-by-default access control (Section 12): only health/OpenAPI docs (and,
 * in the 'local' profile, the fallback login endpoint) are public - everything
 * else under /api/** requires an authenticated request, with role checks via
 * @PreAuthorize on individual controller methods as they're built.
 *
 * Exactly one of the two SecurityFilterChain beans below is active at a time,
 * selected by the active Spring profile.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/actuator/health/**", "/actuator/info",
        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
        "/api/v1/status"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile("local")
    public SecurityFilterChain localFilterChain(HttpSecurity http, LocalJwtService jwtService) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/api/**"), new AntPathRequestMatcher("/h2-console/**")))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // H2 console renders in a frame
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // dev-only DB console, never present in the 'azure' profile
                .anyRequest().authenticated())
            .addFilterBefore(new LocalJwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Profile("azure")
    public SecurityFilterChain azureFilterChain(HttpSecurity http, EntraJwtRoleConverter entraJwtRoleConverter) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/api/**")))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(entraJwtRoleConverter)));
        return http.build();
    }
}
