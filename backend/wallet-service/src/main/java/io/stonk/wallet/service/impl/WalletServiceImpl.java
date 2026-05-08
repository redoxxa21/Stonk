package io.stonk.wallet.service.impl;

import io.stonk.wallet.security.JwtUser;
import io.stonk.wallet.dto.TransactionResponse;
import io.stonk.wallet.dto.WalletResponse;
import io.stonk.wallet.entity.Transaction;
import io.stonk.wallet.entity.TransactionType;
import io.stonk.wallet.entity.Wallet;
import io.stonk.wallet.exception.InsufficientFundsException;
import io.stonk.wallet.exception.WalletAlreadyExistsException;
import io.stonk.wallet.exception.WalletNotFoundException;
import io.stonk.wallet.repository.TransactionRepository;
import io.stonk.wallet.repository.WalletRepository;
import io.stonk.wallet.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final String defaultCurrency;
    private final BigDecimal defaultBalance;

    public WalletServiceImpl(WalletRepository walletRepository,
                             TransactionRepository transactionRepository,
                             @Value("${wallet.default-currency:USD}") String defaultCurrency,
                             @Value("${wallet.default-balance:0.00}") BigDecimal defaultBalance) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.defaultCurrency = defaultCurrency;
        this.defaultBalance = defaultBalance;
    }

    @Override
    @Transactional
    public WalletResponse createWallet(Long userId) {
        JwtUser user = validateUserAccess(userId);
        if (walletRepository.existsByUserId(userId)) {
            throw new WalletAlreadyExistsException(userId);
        }
        Wallet wallet = Wallet.builder()
                .userId(user.id())
                .username(user.username())
                .balance(defaultBalance)
                .currency(defaultCurrency)
                .build();
        walletRepository.save(wallet);
        log.info("Created wallet for userId: {} username: {}", user.id(), user.username());
        return toResponse(wallet);
    }

    @Override
    public WalletResponse getWallet(Long userId) {
        return toResponse(findByUserId(userId));
    }

    @Override
    @Transactional
    public WalletResponse deposit(Long userId, BigDecimal amount) {
        Wallet wallet = findByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        recordTransaction(wallet, TransactionType.DEPOSIT, amount, "Deposit");
        log.info("Deposited {} into wallet for userId: {}", amount, userId);
        return toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse withdraw(Long userId, BigDecimal amount) {
        Wallet wallet = findByUserId(userId);
        ensureFunds(wallet, amount);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        recordTransaction(wallet, TransactionType.WITHDRAWAL, amount, "Withdrawal");
        log.info("Withdrew {} from wallet for userId: {}", amount, userId);
        return toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse debit(Long userId, BigDecimal amount, String description) {
        Wallet wallet = findByUserId(userId);
        ensureFunds(wallet, amount);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        recordTransaction(wallet, TransactionType.TRADE_DEBIT, amount, description);
        log.info("Trade debit {} for userId: {} — {}", amount, userId, description);
        return toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse credit(Long userId, BigDecimal amount, String description) {
        Wallet wallet = findByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        recordTransaction(wallet, TransactionType.TRADE_CREDIT, amount, description);
        log.info("Trade credit {} for userId: {} — {}", amount, userId, description);
        return toResponse(wallet);
    }

    @Override
    public List<TransactionResponse> getTransactions(Long userId) {
        Wallet wallet = findByUserId(userId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream().map(this::toTxResponse).toList();
    }

    // ── Helpers ──

    private Wallet findByUserId(Long userId) {
        return walletRepository.findByUserId(userId).orElseThrow(() -> new WalletNotFoundException(userId));
    }

    private void ensureFunds(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) throw new InsufficientFundsException();
    }

    private void recordTransaction(Wallet wallet, TransactionType type, BigDecimal amount, String desc) {
        transactionRepository.save(Transaction.builder()
                .walletId(wallet.getId()).type(type).amount(amount)
                .balanceAfter(wallet.getBalance()).description(desc).build());
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder().id(w.getId()).userId(w.getUserId()).username(w.getUsername())
                .balance(w.getBalance()).currency(w.getCurrency()).build();
    }

    private TransactionResponse toTxResponse(Transaction t) {
        return TransactionResponse.builder().id(t.getId()).type(t.getType()).amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter()).description(t.getDescription())
                .createdAt(t.getCreatedAt()).build();
    }

    private JwtUser validateUserAccess(Long userId) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUser jwtUser)) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        if (!userId.equals(jwtUser.id())) {
            throw new AccessDeniedException("You can only access your own wallet");
        }
        return jwtUser;
    }
}
