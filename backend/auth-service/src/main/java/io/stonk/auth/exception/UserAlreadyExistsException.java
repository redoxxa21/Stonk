package io.stonk.auth.exception;

/**
 * Thrown when a registration attempt uses a username or email
 * that is already associated with an existing account.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public static UserAlreadyExistsException byEmail(String email) {
        return new UserAlreadyExistsException("Email address is already registered: " + email);
    }

    public static UserAlreadyExistsException byUsername(String username) {
        return new UserAlreadyExistsException("Username is already taken: " + username);
    }
}
