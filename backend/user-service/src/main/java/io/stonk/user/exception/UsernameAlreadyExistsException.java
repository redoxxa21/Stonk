package io.stonk.user.exception;

/**
 * Thrown when a client attempts to assign a username that is
 * already associated with another user account.
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username is already taken: " + username);
    }
}
