package io.stonk.trading.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * HTTP client for the Wallet Service.
 * Base URL is injected from {@code service.wallet.base-url} in application.yaml.
 */
@Slf4j
@Component
public class WalletClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public WalletClient(RestTemplate restTemplate,
                        @Value("${service.wallet.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void debit(Long userId, BigDecimal amount, String authHeader) {
        HttpHeaders headers = jsonHeaders(authHeader);
        Map<String, Object> body = Map.of("amount", amount);
        restTemplate.exchange(baseUrl + "/wallet/" + userId + "/debit",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        log.debug("Debited {} from wallet for userId:{}", amount, userId);
    }

    public void credit(Long userId, BigDecimal amount, String authHeader) {
        HttpHeaders headers = jsonHeaders(authHeader);
        Map<String, Object> body = Map.of("amount", amount);
        restTemplate.exchange(baseUrl + "/wallet/" + userId + "/credit",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        log.debug("Credited {} to wallet for userId:{}", amount, userId);
    }

    private HttpHeaders jsonHeaders(String authHeader) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set(HttpHeaders.AUTHORIZATION, authHeader);
        return h;
    }
}
