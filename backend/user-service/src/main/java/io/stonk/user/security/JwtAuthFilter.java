package io.stonk.user.security;

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
 * <p>This service assumes that the API Gateway has already validated the JWT.
 * This filter adds a second layer of validation and populates the
 * {@link org.springframework.security.core.context.SecurityContext} so that
 * downstream code (e.g. {@code @PreAuthorize}) can inspect the authenticated user.
 *
 * <p>If no valid token is present the request is still forwarded — endpoint-level
 * security rules in {@link io.stonk.user.config.SecurityConfig} determine
 * whether an unauthenticated request is accepted.
 *
 * <p>This filter is intentionally self-contained: it does NOT query the
 * database for user lookup, which keeps it stateless and fast.
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
            // already authenticated earlier in the chain — nothing to do
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtService.isTokenValid(token, username)) {
            /*
             * We extract the role claim from the token rather than hitting the DB,
             * keeping this filter stateless. The "ROLE_" prefix is required by Spring Security.
             */
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
            log.debug("Invalid JWT for request to {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}