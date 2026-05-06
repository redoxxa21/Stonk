package io.stonk.user.exception;

/**
 * Thrown when a requested user cannot be found in the database.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException byId(Long id) {
        return new UserNotFoundException("User not found with id: " + id);
    }

    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("User not found with username: " + username);
    }
}
