package io.stonk.trading.client;

import io.stonk.trading.dto.UserLookupResponse;

public interface UserDirectoryClient {
    UserLookupResponse getUserById(Long userId, String bearerToken);
}
