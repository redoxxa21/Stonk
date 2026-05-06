package io.stonk.portfolio.exception;

public class WalletServiceUnauthorizedException extends RuntimeException {
    public WalletServiceUnauthorizedException() {
        super("Wallet service rejected the authentication token");
    }
}
