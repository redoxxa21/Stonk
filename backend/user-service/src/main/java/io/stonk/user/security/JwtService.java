package io.stonk.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.function.Function;

/**
 * Stateless JWT utility component.
 *
 * <p>Reads tokens signed by the Auth Service using the shared secret.
 * Validation only — this service never issues tokens.
 *
 * <p>The secret key is externalised via {@code jwt.secret-key} in
 * {@code application.yaml} and must match the Auth Service value exactly.
 */
@Slf4j
@Component
public class JwtService {

    private final String secretKey;

    public JwtService(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    // ────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────

    /**
     * Extracts the {@code sub} (subject / username) claim from the token.
     *
     * @param token the raw JWT string (without "Bearer " prefix)
     * @return the username, or {@code null} if the token is invalid
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Failed to extract username from token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Validates the token signature and checks that the embedded username
     * matches the expected value.
     *
     * @param token    the raw JWT string
     * @param username the username to match against the token subject
     * @return {@code true} if the token is valid and belongs to {@code username}
     */
    public boolean isTokenValid(String token, String username) {
        try {
            String subject = extractClaim(token, Claims::getSubject);
            return username != null && username.equals(subject);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extracts an arbitrary claim from the token using the provided resolver.
     *
     * @param token    the raw JWT string
     * @param resolver a function from {@link Claims} to the desired value
     * @param <T>      the type of the claim value
     * @return the extracted claim value
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
