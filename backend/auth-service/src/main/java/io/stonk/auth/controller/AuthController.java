package io.stonk.auth.controller;

import io.stonk.auth.dto.AuthResponse;
import io.stonk.auth.dto.LoginRequest;
import io.stonk.auth.dto.RegisterRequest;
import io.stonk.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing authentication endpoints.
 *
 * <p>This class is a pure HTTP adapter — it delegates every operation to
 * {@link AuthService} and contains zero business logic.
 *
 * <p>All endpoints under {@code /auth/**} are publicly accessible
 * (no JWT required) as configured in {@link io.stonk.auth.config.SecurityConfig}.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ────────────────────────────────────────────────
    // POST /auth/register
    // ────────────────────────────────────────────────

    /**
     * Registers a new user account.
     *
     * <p>On success the caller receives a JWT immediately — no separate
     * login step required after registration.
     *
     * @param request validated registration payload
     * @return 201 Created with the auth response (token + identity)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("POST /auth/register for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ────────────────────────────────────────────────
    // POST /auth/login
    // ────────────────────────────────────────────────

    /**
     * Authenticates an existing user.
     *
     * @param request validated login payload
     * @return 200 OK with the auth response (token + identity)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("POST /auth/login for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}