package io.stonk.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * Gateway-level JWT validator.
 * Only validates the token signature and expiration — does NOT generate tokens.
 */
@Slf4j
@Component
public class JwtService {

    private final String secretKey;

    public JwtService(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Returns true if the token can be parsed and its signature is valid.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT at gateway: {}", ex.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
