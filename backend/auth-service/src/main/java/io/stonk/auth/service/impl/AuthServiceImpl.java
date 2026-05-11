package io.stonk.auth.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.auth.dto.AuthResponse;
import io.stonk.auth.dto.LoginRequest;
import io.stonk.auth.dto.RegisterRequest;
import io.stonk.auth.dto.UserRegisteredEvent;
import io.stonk.auth.entity.Role;
import io.stonk.auth.entity.User;
import io.stonk.auth.exception.InvalidCredentialsException;
import io.stonk.auth.exception.UserAlreadyExistsException;
import io.stonk.auth.kafka.AuthDomainTopics;
import io.stonk.auth.repository.UserRepository;
import io.stonk.auth.security.JwtService;
import io.stonk.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Default implementation of {@link AuthService}.
 *
 * <p>Public registration creates standard users only. Administrative access is
 * provisioned via startup seeding rather than open sign-up input.
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

        Role role = resolvePublicRegistrationRole(request.getRole());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("New user registered: '{}' with role {}", user.getUsername(), role);

        User committedUser = user;
        runAfterCommit(() -> publishUserRegistered(committedUser));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .message("Registration successful")
                .build();
    }

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

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private void publishUserRegistered(User user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(AuthDomainTopics.USER_REGISTRATION, String.valueOf(user.getId()), json);
        } catch (JsonProcessingException ex) {
            log.warn("Could not serialize user-registration event for user {}: {}", user.getId(), ex.getMessage());
        } catch (Exception ex) {
            log.warn("Could not publish user-registration event for user {}: {}", user.getId(), ex.getMessage());
        }
    }

    private Role resolvePublicRegistrationRole(String roleString) {
        if (roleString != null && !roleString.isBlank() && !"USER".equalsIgnoreCase(roleString.trim())) {
            log.warn("Public registration attempted role '{}' - forcing USER", roleString);
        }
        return Role.USER;
    }
}
