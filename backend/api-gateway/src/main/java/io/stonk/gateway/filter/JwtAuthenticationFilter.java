package io.stonk.gateway.filter;

import io.stonk.gateway.security.JwtService;
import io.stonk.gateway.config.GatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive global filter that validates JWT tokens on every request
 * except those matching {@code gateway.open-paths}.
 *
 * <p>If the token is missing or invalid, the request is rejected
 * with {@code 401 Unauthorized} before reaching any downstream service.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private final List<String> openPaths;

    public JwtAuthenticationFilter(JwtService jwtService, GatewayProperties gatewayProperties) {
        this.jwtService = jwtService;
        this.openPaths = gatewayProperties.getOpenPaths();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip open paths
        if (isOpenPath(path)) {
            return chain.filter(exchange);
        }

        // Require Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            log.warn("Invalid JWT for path: {}", path);
            return unauthorized(exchange);
        }

        // Valid — pass through (downstream services also validate)
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }

    private boolean isOpenPath(String path) {
        return openPaths.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
