package io.stonk.auth.service.impl;

import io.stonk.auth.dto.AuthResponse;
import io.stonk.auth.dto.LoginRequest;
import io.stonk.auth.dto.RegisterRequest;
import io.stonk.auth.entity.Role;
import io.stonk.auth.entity.User;
import io.stonk.auth.exception.InvalidCredentialsException;
import io.stonk.auth.exception.UserAlreadyExistsException;
import io.stonk.auth.repository.UserRepository;
import io.stonk.auth.security.JwtService;
import io.stonk.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.auth.dto.UserRegisteredEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AuthService}.
 *
 * <p>All business logic lives here — the controller is a thin HTTP adapter.
 * Constructor-based injection is used exclusively (no field injection).
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           KafkaTemplate<String, String> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // ────────────────────────────────────────────────
    // Registration
    // ────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Checks for existing username and email before persisting, then
     * returns a JWT so the client is immediately authenticated after sign-up.
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Processing registration for username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw UserAlreadyExistsException.byUsername(request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserAlreadyExistsException.byEmail(request.getEmail());
        }

        Role role = resolveRole(request.getRole());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("New user registered: '{}' with role {}", user.getUsername(), role);

        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-registration", eventJson);
            log.debug("Published UserRegisteredEvent to Kafka for {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish UserRegisteredEvent to Kafka for {}", user.getUsername(), e);
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .message("Registration successful")
                .build();
    }

    // ────────────────────────────────────────────────
    // Login
    // ────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Validates credentials and returns a fresh JWT. The response is
     * deliberately identical for unknown-user and wrong-password cases to
     * prevent username enumeration.
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.debug("Processing login for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for username: {}", request.getUsername());
            throw new InvalidCredentialsException();
        }

        log.info("Successful login for user: '{}'", user.getUsername());

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }

    // ────────────────────────────────────────────────
    // Private helpers
    // ────────────────────────────────────────────────

    /**
     * Resolves the requested role string to a {@link Role} enum value.
     *
     * <p>Defaults to {@link Role#USER} when the role field is absent or blank.
     * Unknown role strings fall back to {@link Role#USER} with a warning,
     * preventing clients from accidentally escalating privileges via typos.
     *
     * @param roleString the raw role value from the request (may be null/blank)
     * @return the resolved role
     */
    private Role resolveRole(String roleString) {
        if (roleString == null || roleString.isBlank()) {
            return Role.USER;
        }
        try {
            return Role.valueOf(roleString.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown role '{}' requested — defaulting to USER", roleString);
            return Role.USER;
        }
    }
}
