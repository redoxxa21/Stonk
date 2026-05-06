package io.stonk.auth.service;

import io.stonk.auth.dto.LoginRequest;
import io.stonk.auth.dto.RegisterRequest;
import io.stonk.auth.entity.Role;
import io.stonk.auth.entity.User;
import io.stonk.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Default role
        Role role = Role.USER;

        // Optional: allow role assignment (e.g., ADMIN)
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            role = Role.valueOf(request.getRole().toUpperCase());
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        // Return JWT
        return jwtService.generateToken(user.getUsername());
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtService.generateToken(user.getUsername());
    }
}