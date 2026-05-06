package io.stonk.portfolio.exception;

public class UserAccessDeniedException extends RuntimeException {
    public UserAccessDeniedException(Long userId) {
        super("Authenticated user is not allowed to access user id: " + userId);
    }
}
