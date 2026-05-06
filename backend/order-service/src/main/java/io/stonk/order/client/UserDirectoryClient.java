package io.stonk.order.client;

import io.stonk.order.dto.UserLookupResponse;

public interface UserDirectoryClient {
    UserLookupResponse getUserById(Long userId, String bearerToken);
}
