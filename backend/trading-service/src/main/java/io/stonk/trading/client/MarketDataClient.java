package io.stonk.trading.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * HTTP client for the Market Data Service.
 * Base URL is injected from {@code service.market-data.base-url} in application.yaml.
 */
@Slf4j
@Component
public class MarketDataClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public MarketDataClient(RestTemplate restTemplate,
                            @Value("${service.market-data.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @SuppressWarnings("unchecked")
    public BigDecimal getCurrentPrice(String symbol, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/market/stocks/" + symbol.toUpperCase(),
                HttpMethod.GET, entity, Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null || body.get("currentPrice") == null) {
            throw new RuntimeException("Could not fetch price for " + symbol);
        }
        return new BigDecimal(body.get("currentPrice").toString());
    }
}
