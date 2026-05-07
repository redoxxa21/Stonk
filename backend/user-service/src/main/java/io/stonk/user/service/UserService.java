package io.stonk.user.service;

import io.stonk.user.dto.UpdateUserRequest;
import io.stonk.user.dto.UserResponse;

import java.util.List;

/**
 * Contract for all user-management operations.
 *
 * <p>Programming to this interface (Open/Closed Principle) allows the
 * implementation to be swapped or decorated without touching controllers.
 */
public interface UserService {

    /**
     * Retrieves a single user by their primary-key id.
     *
     * @param id the user's database id
     * @return the user profile — never contains a password
     * @throws io.stonk.user.exception.UserNotFoundException if no user exists with the given id
     */
    UserResponse getUserById(Long id);

    /**
     * Retrieves a single user by their username.
     *
     * @param username the exact username
     * @return the user profile
     * @throws io.stonk.user.exception.UserNotFoundException if no user exists with the given username
     */
    UserResponse getUserByUsername(String username);

    /**
     * Returns all registered users.
     *
     * @return an unmodifiable list of user profiles
     */
    List<UserResponse> getAllUsers();
 
    /**
     * Updates the mutable fields (username / email) of an existing user.
     *
     * <p>Only fields present in {@code request} that are non-null will be applied.
     *
     * @param id      the user's database id
     * @param request the partial update payload
     * @return the updated user profile
     * @throws io.stonk.user.exception.UserNotFoundException          if no user exists with the given id
     * @throws io.stonk.user.exception.UsernameAlreadyExistsException if the new username is taken
     * @throws io.stonk.user.exception.EmailAlreadyExistsException    if the new email is taken
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Permanently deletes a user record.
     *
     * @param id the user's database id
     * @throws io.stonk.user.exception.UserNotFoundException if no user exists with the given id
     */
    void deleteUser(Long id);
}