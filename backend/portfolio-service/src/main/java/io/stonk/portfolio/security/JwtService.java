package io.stonk.portfolio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.function.Function;

@Slf4j @Component
public class JwtService {
    private final String secretKey;
    public JwtService(@Value("${jwt.secret-key}") String secretKey) { this.secretKey = secretKey; }
    public String extractUsername(String token) {
        try { return extractClaim(token, Claims::getSubject); } catch (JwtException | IllegalArgumentException ex) { return null; }
    }
    public boolean isTokenValid(String token, String username) {
        try { return username != null && username.equals(extractClaim(token, Claims::getSubject)); } catch (JwtException | IllegalArgumentException ex) { return false; }
    }
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token).getBody());
    }
    private Key signingKey() { return Keys.hmacShaKeyFor(secretKey.getBytes()); }
}
