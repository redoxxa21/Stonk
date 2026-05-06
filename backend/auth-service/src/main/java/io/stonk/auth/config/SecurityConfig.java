package io.stonk.auth.config;

import io.stonk.auth.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Auth Service.
 *
 * <p>Key rules:
 * <ul>
 *   <li>{@code /auth/**} — publicly accessible (register and login require no token)</li>
 *   <li>{@code /actuator/health}, {@code /actuator/info} — public for health checks</li>
 *   <li>All other requests — require a valid JWT</li>
 *   <li>Unmatched routes — denied by default (fail-closed posture)</li>
 * </ul>
 *
 * <p>No {@code AuthenticationManager} bean is needed — this service authenticates
 * users manually in the service layer via {@link PasswordEncoder} + repository lookup.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ── CSRF disabled: stateless REST API, no browser sessions ──
            .csrf(AbstractHttpConfigurer::disable)

            // ── No HTTP sessions — every request is self-contained ──
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── Authorisation rules ──
            .authorizeHttpRequests(auth -> auth

                // Registration and login are open to the world
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                // Health checks are public for infrastructure tooling
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()

                // Any future protected endpoints require authentication
                .anyRequest().authenticated()
            )

            // ── JWT filter ──
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder — the standard choice for credential hashing.
     *
     * <p>BCrypt's built-in salt and cost factor make it resistant to
     * rainbow-table and brute-force attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}