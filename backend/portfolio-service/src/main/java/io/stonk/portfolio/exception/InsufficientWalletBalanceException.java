package io.stonk.portfolio.exception;

import java.math.BigDecimal;

public class InsufficientWalletBalanceException extends RuntimeException {
    public InsufficientWalletBalanceException(BigDecimal required, BigDecimal available) {
        super("Insufficient wallet balance. Required: " + required + ", available: " + available);
    }
}
