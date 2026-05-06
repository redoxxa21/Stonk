package io.stonk.wallet.exception;

public class WalletAlreadyExistsException extends RuntimeException {
    public WalletAlreadyExistsException(Long userId) { super("Wallet already exists for user id: " + userId); }
}
