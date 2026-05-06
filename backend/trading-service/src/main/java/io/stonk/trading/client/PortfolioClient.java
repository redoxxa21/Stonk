package io.stonk.trading.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * HTTP client for the Portfolio Service.
 * Base URL is injected from {@code service.portfolio.base-url} in application.yaml.
 */
@Slf4j
@Component
public class PortfolioClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PortfolioClient(RestTemplate restTemplate,
                           @Value("${service.portfolio.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void addHolding(Long userId, String symbol, int quantity, BigDecimal price, String authHeader) {
        HttpHeaders headers = jsonHeaders(authHeader);
        Map<String, Object> body = Map.of("symbol", symbol, "quantity", quantity, "price", price);
        restTemplate.exchange(baseUrl + "/portfolio/" + userId + "/buy",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        log.debug("Added {} shares of {} for userId:{}", quantity, symbol, userId);
    }

    public void reduceHolding(Long userId, String symbol, int quantity, BigDecimal price, String authHeader) {
        HttpHeaders headers = jsonHeaders(authHeader);
        Map<String, Object> body = Map.of("symbol", symbol, "quantity", quantity, "price", price);
        restTemplate.exchange(baseUrl + "/portfolio/" + userId + "/sell",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        log.debug("Reduced {} shares of {} for userId:{}", quantity, symbol, userId);
    }

    private HttpHeaders jsonHeaders(String authHeader) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set(HttpHeaders.AUTHORIZATION, authHeader);
        return h;
    }
}
