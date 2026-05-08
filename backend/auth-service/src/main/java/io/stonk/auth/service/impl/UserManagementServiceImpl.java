package io.stonk.auth.service.impl;

import io.stonk.auth.dto.UpdateUserRequest;
import io.stonk.auth.dto.UserResponse;
import io.stonk.auth.entity.User;
import io.stonk.auth.exception.UserAlreadyExistsException;
import io.stonk.auth.exception.UserNotFoundException;
import io.stonk.auth.repository.UserRepository;
import io.stonk.auth.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementServiceImpl implements UserManagementService {
    private final UserRepository userRepository;

    @Override
    public UserResponse getUserById(Long id) {
        return toResponse(userRepository.findById(id).orElseThrow(() -> UserNotFoundException.byId(id)));
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        return toResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.byUsername(username)));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> UserNotFoundException.byId(id));

        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw UserAlreadyExistsException.byUsername(request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw UserAlreadyExistsException.byEmail(request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException.byId(id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user {}", id);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
