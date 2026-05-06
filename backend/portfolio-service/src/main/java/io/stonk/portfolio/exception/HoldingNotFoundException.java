package io.stonk.portfolio.exception;

public class HoldingNotFoundException extends RuntimeException {
    public HoldingNotFoundException(Long userId, String symbol) {
        super("No holding found for user " + userId + " and symbol " + symbol);
    }
}
