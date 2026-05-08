package io.stonk.auth.service;

import io.stonk.auth.dto.UpdateUserRequest;
import io.stonk.auth.dto.UserResponse;

import java.util.List;

public interface UserManagementService {
    UserResponse getUserById(Long id);
    UserResponse getUserByUsername(String username);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}
