package io.stonk.user.service.impl;

import io.stonk.user.dto.UpdateUserRequest;
import io.stonk.user.dto.UserResponse;
import io.stonk.user.entity.User;
import io.stonk.user.exception.EmailAlreadyExistsException;
import io.stonk.user.exception.UserNotFoundException;
import io.stonk.user.exception.UsernameAlreadyExistsException;
import io.stonk.user.mapper.UserMapper;
import io.stonk.user.repository.UserRepository;
import io.stonk.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link UserService}.
 *
 * <p>All business logic lives here — controllers remain thin HTTP adapters.
 * Constructor injection is used exclusively (no field injection).
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    // ────────────────────────────────────────────────
    // Read operations (readOnly = true → performance hint to JPA)
    // ────────────────────────────────────────────────

    @Override
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.byUsername(username));
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // ────────────────────────────────────────────────
    // Write operations
    // ────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.debug("Updating user id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));

        applyUsernameUpdate(request, user);
        applyEmailUpdate(request, user);

        User saved = userRepository.save(user);
        log.info("User id {} updated successfully", id);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user id: {}", id);

        if (!userRepository.existsById(id)) {
            throw UserNotFoundException.byId(id);
        }

        userRepository.deleteById(id);
        log.info("User id {} deleted successfully", id);
    }

    // ────────────────────────────────────────────────
    // Private helpers (extracted for SRP / readability)
    // ────────────────────────────────────────────────

    /**
     * Applies the username change from the request if supplied and not already taken.
     */
    private void applyUsernameUpdate(UpdateUserRequest request, User user) {
        String newUsername = request.getUsername();
        if (newUsername == null || newUsername.isBlank()) {
            return;
        }
        if (newUsername.equals(user.getUsername())) {
            return; // no change needed
        }
        if (userRepository.existsByUsername(newUsername)) {
            throw new UsernameAlreadyExistsException(newUsername);
        }
        user.setUsername(newUsername);
    }

    /**
     * Applies the email change from the request if supplied and not already taken.
     */
    private void applyEmailUpdate(UpdateUserRequest request, User user) {
        String newEmail = request.getEmail();
        if (newEmail == null || newEmail.isBlank()) {
            return;
        }
        if (newEmail.equals(user.getEmail())) {
            return; // no change needed
        }
        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException(newEmail);
        }
        user.setEmail(newEmail);
    }
}
