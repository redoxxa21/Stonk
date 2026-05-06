package io.stonk.auth.exception;

/**
 * Thrown when login credentials are incorrect.
 *
 * <p>The message is intentionally generic — we do not indicate whether the
 * username or password was wrong, to prevent user-enumeration attacks.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
