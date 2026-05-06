package io.stonk.user.config;

import io.stonk.user.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the User Service.
 *
 * <p><strong>Design rationale:</strong>
 * The API Gateway is responsible for primary JWT validation before requests
 * reach this service. This filter chain provides a second layer of defence and
 * populates the {@link org.springframework.security.core.context.SecurityContext}
 * so that {@code @PreAuthorize} annotations work if added in the future.
 *
 * <p>All {@code /users/**} endpoints require a valid Bearer token. The
 * {@code /actuator/health} endpoint is intentionally public so that the gateway
 * and orchestration tooling (e.g. Docker/Kubernetes) can perform health checks.
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
            // ── CSRF disabled: stateless REST API uses JWTs, not sessions ──
            .csrf(AbstractHttpConfigurer::disable)

            // ── Stateless session: no HttpSession is created or used ──
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── Authorisation rules ──
            .authorizeHttpRequests(auth -> auth

                // Health check is publicly accessible
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()

                // All user endpoints require authentication
                .requestMatchers("/users/**").authenticated()

                // Deny anything not explicitly mapped
                .anyRequest().denyAll()
            )

            // ── JWT filter runs before Spring's username/password filter ──
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}