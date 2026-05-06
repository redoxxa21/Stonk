package io.stonk.user.controller;

import io.stonk.user.dto.UpdateUserRequest;
import io.stonk.user.dto.UserResponse;
import io.stonk.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing user-management endpoints.
 *
 * <p>This class is a pure HTTP adapter — it delegates every operation to
 * {@link UserService} and contains zero business logic.
 *
 * <p>All endpoints return {@link UserResponse} which never exposes passwords.
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ────────────────────────────────────────────────
    // GET /users/{id}
    // ────────────────────────────────────────────────

    /**
     * Returns a single user profile by id.
     *
     * @param id the user's primary key
     * @return 200 with the user profile, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.debug("GET /users/{}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ────────────────────────────────────────────────
    // GET /users/username/{username}
    // ────────────────────────────────────────────────

    /**
     * Returns a single user profile by username.
     *
     * @param username the exact username to look up
     * @return 200 with the user profile, or 404 if not found
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        log.debug("GET /users/username/{}", username);
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    // ────────────────────────────────────────────────
    // GET /users
    // ────────────────────────────────────────────────

    /**
     * Returns all registered users.
     *
     * @return 200 with a list of user profiles (may be empty)
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.debug("GET /users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ────────────────────────────────────────────────
    // PUT /users/{id}
    // ────────────────────────────────────────────────

    /**
     * Updates the mutable fields (username / email) of a user.
     *
     * <p>Password and role updates are intentionally not supported here.
     *
     * @param id      the user's primary key
     * @param request the fields to update (both optional)
     * @return 200 with the updated profile, 404 if not found, 409 on conflict
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.debug("PUT /users/{}", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ────────────────────────────────────────────────
    // DELETE /users/{id}
    // ────────────────────────────────────────────────

    /**
     * Permanently deletes a user.
     *
     * @param id the user's primary key
     * @return 204 No Content on success, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.debug("DELETE /users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}