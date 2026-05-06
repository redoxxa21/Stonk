package io.stonk.order.exception;

public class UserServiceUnauthorizedException extends RuntimeException {
    public UserServiceUnauthorizedException() {
        super("User service rejected the authentication token");
    }
}
