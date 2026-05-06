package io.stonk.wallet.service;

import io.stonk.wallet.dto.TransactionResponse;
import io.stonk.wallet.dto.WalletResponse;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    WalletResponse createWallet(Long userId, String bearerToken);
    WalletResponse getWallet(Long userId);
    WalletResponse deposit(Long userId, BigDecimal amount);
    WalletResponse withdraw(Long userId, BigDecimal amount);
    WalletResponse debit(Long userId, BigDecimal amount, String description);
    WalletResponse credit(Long userId, BigDecimal amount, String description);
    List<TransactionResponse> getTransactions(Long userId);
}
