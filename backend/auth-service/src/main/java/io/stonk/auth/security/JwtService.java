package io.stonk.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.stonk.auth.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Responsible for generating and validating JWT tokens.
 *
 * <p>This is the <strong>only</strong> place in the system where tokens are
 * issued. Downstream services (User Service, API Gateway) only validate them.
 *
 * <p>The token embeds:
 * <ul>
 *   <li>{@code sub} — username (subject)</li>
 *   <li>{@code role} — the user's role (e.g. {@code USER}, {@code ADMIN})</li>
 *   <li>{@code iat} — issued-at timestamp</li>
 *   <li>{@code exp} — expiration timestamp</li>
 * </ul>
 *
 * <p>Both the secret key and expiration are externalised to {@code application.yaml}
 * so they can be rotated without source changes.
 */
@Slf4j
@Component
public class JwtService {

    private final String secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    // ────────────────────────────────────────────────
    // Token generation
    // ────────────────────────────────────────────────

    /**
     * Generates a signed JWT for the given user.
     *
     * <p>The {@code role} claim is embedded so downstream services can make
     * authorisation decisions without querying the database.
     *
     * @param user the authenticated or newly registered user
     * @return a compact, signed JWT string
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ────────────────────────────────────────────────
    // Token validation
    // ────────────────────────────────────────────────

    /**
     * Extracts the subject (username) from the token.
     *
     * @param token raw JWT string (without "Bearer " prefix)
     * @return the username, or {@code null} if the token is invalid
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Could not extract username from token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Validates that the token is correctly signed, not expired,
     * and belongs to the expected username.
     *
     * @param token    raw JWT string
     * @param username the expected subject
     * @return {@code true} if the token is fully valid
     */
    public boolean isTokenValid(String token, String username) {
        try {
            String subject = extractClaim(token, Claims::getSubject);
            Date expiration = extractClaim(token, Claims::getExpiration);
            return username != null
                    && username.equals(subject)
                    && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extracts an arbitrary claim from the token using the provided resolver.
     *
     * @param token    raw JWT string
     * @param resolver function from {@link Claims} to the desired value
     * @param <T>      the claim value type
     * @return the extracted value
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // ────────────────────────────────────────────────
    // Private helpers
    // ────────────────────────────────────────────────

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
