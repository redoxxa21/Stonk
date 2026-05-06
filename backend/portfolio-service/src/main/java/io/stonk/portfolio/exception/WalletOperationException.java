package io.stonk.portfolio.exception;

public class WalletOperationException extends RuntimeException {
    public WalletOperationException(String message) {
        super(message);
    }
}
