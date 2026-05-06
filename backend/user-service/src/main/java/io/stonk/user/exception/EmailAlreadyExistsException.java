package io.stonk.user.exception;

/**
 * Thrown when a client attempts to assign an email address that is
 * already associated with another user account.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email address is already in use: " + email);
    }
}
