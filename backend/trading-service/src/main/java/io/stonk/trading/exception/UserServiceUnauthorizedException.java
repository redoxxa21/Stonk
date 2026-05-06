package io.stonk.trading.exception;

public class UserServiceUnauthorizedException extends RuntimeException {
    public UserServiceUnauthorizedException() {
        super("User service rejected the authentication token");
    }
}
