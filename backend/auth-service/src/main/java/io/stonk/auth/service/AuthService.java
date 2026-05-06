package io.stonk.auth.service;

import io.stonk.auth.dto.AuthResponse;
import io.stonk.auth.dto.LoginRequest;
import io.stonk.auth.dto.RegisterRequest;

/**
 * Contract for all authentication operations.
 *
 * <p>Programming to an interface (Dependency Inversion Principle) allows the
 * implementation to be swapped or decorated without touching the controller.
 */
public interface AuthService {

    /**
     * Registers a new user and returns a JWT on success.
     *
     * @param request the registration payload
     * @return an {@link AuthResponse} containing the JWT and user identity
     * @throws io.stonk.auth.exception.UserAlreadyExistsException if the username or email is taken
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user and returns a JWT on success.
     *
     * @param request the login payload
     * @return an {@link AuthResponse} containing the JWT and user identity
     * @throws io.stonk.auth.exception.InvalidCredentialsException if credentials are incorrect
     */
    AuthResponse login(LoginRequest request);
}