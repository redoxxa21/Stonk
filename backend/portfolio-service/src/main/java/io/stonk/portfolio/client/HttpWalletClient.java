package io.stonk.portfolio.client;

import io.stonk.portfolio.dto.WalletLookupResponse;
import io.stonk.portfolio.exception.WalletOperationException;
import io.stonk.portfolio.exception.WalletNotFoundException;
import io.stonk.portfolio.exception.WalletServiceUnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class HttpWalletClient implements WalletClient {

    private final RestTemplate restTemplate;
    private final String walletServiceBaseUrl;

    public HttpWalletClient(RestTemplate restTemplate,
                            @Value("${portfolio.wallet-service-base-url:http://localhost:2500}") String walletServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.walletServiceBaseUrl = walletServiceBaseUrl;
    }

    @Override
    public WalletLookupResponse getWallet(Long userId, String bearerToken) {
        return exchangeWallet(userId, bearerToken, HttpMethod.GET, "/wallet/{id}", null);
    }

    @Override
    public WalletLookupResponse debit(Long userId, BigDecimal amount, String bearerToken) {
        return exchangeWallet(userId, bearerToken, HttpMethod.POST, "/wallet/{id}/debit", Map.of("amount", amount));
    }

    @Override
    public WalletLookupResponse credit(Long userId, BigDecimal amount, String bearerToken) {
        return exchangeWallet(userId, bearerToken, HttpMethod.POST, "/wallet/{id}/credit", Map.of("amount", amount));
    }

    private WalletLookupResponse exchangeWallet(Long userId,
                                                String bearerToken,
                                                HttpMethod method,
                                                String path,
                                                Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
            }
            if (body != null) {
                headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
            }
            return restTemplate.exchange(
                    walletServiceBaseUrl + path,
                    method,
                    new HttpEntity<>(body, headers),
                    WalletLookupResponse.class,
                    userId
            ).getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new WalletNotFoundException(userId);
            }
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new WalletServiceUnauthorizedException();
            }
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new WalletOperationException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }
}
