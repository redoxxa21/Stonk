package io.stonk.trading.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * HTTP client for the Order Service.
 * Base URL is injected from {@code service.order.base-url} in application.yaml.
 */
@Slf4j
@Component
public class OrderClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public OrderClient(RestTemplate restTemplate,
                       @Value("${service.order.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @SuppressWarnings("unchecked")
    public Long createOrder(Long userId, String symbol, String type, int quantity, BigDecimal price, String authHeader) {
        HttpHeaders headers = jsonHeaders(authHeader);
        Map<String, Object> body = Map.of(
                "userId", userId, "symbol", symbol, "type", type,
                "quantity", quantity, "price", price);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/orders", HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);
        Map<String, Object> resBody = response.getBody();
        Long orderId = resBody != null ? Long.valueOf(resBody.get("id").toString()) : null;
        log.debug("Created order #{} for userId:{}", orderId, userId);
        return orderId;
    }

    public void completeOrder(Long orderId, String authHeader) {
        HttpHeaders headers = jsonHeaders(authHeader);
        restTemplate.exchange(baseUrl + "/orders/" + orderId + "/complete",
                HttpMethod.PUT, new HttpEntity<>(headers), Map.class);
        log.debug("Completed order #{}", orderId);
    }

    private HttpHeaders jsonHeaders(String authHeader) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set(HttpHeaders.AUTHORIZATION, authHeader);
        return h;
    }
}
