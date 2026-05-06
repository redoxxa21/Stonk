package io.stonk.portfolio.client;

import io.stonk.portfolio.dto.UserLookupResponse;

public interface UserDirectoryClient {
    UserLookupResponse getUserById(Long userId, String bearerToken);
}
