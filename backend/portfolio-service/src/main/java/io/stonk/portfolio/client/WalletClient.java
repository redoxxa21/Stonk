package io.stonk.portfolio.client;

import io.stonk.portfolio.dto.WalletLookupResponse;

import java.math.BigDecimal;

public interface WalletClient {
    WalletLookupResponse getWallet(Long userId, String bearerToken);
    WalletLookupResponse debit(Long userId, BigDecimal amount, String bearerToken);
    WalletLookupResponse credit(Long userId, BigDecimal amount, String bearerToken);
}
