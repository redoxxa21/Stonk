package io.stonk.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter — executes once per request.
 *
 * <p>For the Auth Service, this filter protects any endpoints beyond
 * {@code /auth/**} (e.g. future admin or internal endpoints). It is
 * intentionally <strong>stateless</strong> — it never queries the database.
 * The role is extracted directly from the JWT {@code role} claim.
 *
 * <p>{@code /auth/**} requests bypass this filter entirely because they
 * are permitted in {@link io.stonk.auth.config.SecurityConfig}.
 */
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());
        final String username = jwtService.extractUsername(token);

        if (username == null) {
            log.debug("Could not extract username from token on {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtService.isTokenValid(token, username)) {
            String roleClaim = jwtService.extractClaim(token,
                    claims -> claims.get("role", String.class));

            List<SimpleGrantedAuthority> authorities = roleClaim != null
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + roleClaim))
                    : List.of();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("Authenticated user '{}' on {}", username, request.getRequestURI());
        } else {
            log.debug("Invalid JWT on request to {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}