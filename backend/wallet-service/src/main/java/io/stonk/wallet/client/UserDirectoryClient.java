package io.stonk.wallet.client;

import io.stonk.wallet.dto.UserLookupResponse;

public interface UserDirectoryClient {
    UserLookupResponse getUserById(Long userId, String bearerToken);
}
