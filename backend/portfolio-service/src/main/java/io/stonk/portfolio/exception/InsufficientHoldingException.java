package io.stonk.portfolio.exception;

public class InsufficientHoldingException extends RuntimeException {
    public InsufficientHoldingException(String symbol, int requested, int available) {
        super("Insufficient holding for " + symbol + ": requested " + requested + ", available " + available);
    }
}
