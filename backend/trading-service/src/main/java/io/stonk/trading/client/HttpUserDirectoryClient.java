package io.stonk.trading.client;

import io.stonk.trading.dto.UserLookupResponse;
import io.stonk.trading.exception.UserNotFoundException;
import io.stonk.trading.exception.UserServiceUnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpUserDirectoryClient implements UserDirectoryClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public HttpUserDirectoryClient(RestTemplate restTemplate,
                                   @Value("${service.user.base-url:http://localhost:2700}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @Override
    public UserLookupResponse getUserById(Long userId, String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
            }
            return restTemplate.exchange(
                    userServiceBaseUrl + "/users/{id}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    UserLookupResponse.class,
                    userId
            ).getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundException(userId);
            }
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new UserServiceUnauthorizedException();
            }
            throw ex;
        }
    }
}
