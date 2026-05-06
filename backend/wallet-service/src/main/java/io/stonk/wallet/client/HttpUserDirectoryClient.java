package io.stonk.wallet.client;

import io.stonk.wallet.dto.UserLookupResponse;
import io.stonk.wallet.exception.UserNotFoundException;
import io.stonk.wallet.exception.UserServiceUnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class HttpUserDirectoryClient implements UserDirectoryClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public HttpUserDirectoryClient(RestTemplate restTemplate,
                                   @Value("${wallet.user-service-base-url:http://localhost:2700}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @Override
    public UserLookupResponse getUserById(Long userId, String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(
                    userServiceBaseUrl + "/users/{id}",
                    HttpMethod.GET,
                    entity,
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
