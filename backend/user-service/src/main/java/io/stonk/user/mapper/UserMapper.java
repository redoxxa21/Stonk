package io.stonk.user.mapper;

import io.stonk.user.dto.UserResponse;
import io.stonk.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converts {@link User} entities to outbound DTOs.
 *
 * <p>Centralising mapping logic here follows the Single Responsibility Principle
 * and ensures that the no-password rule is enforced in exactly one place.
 */
@Component
public class UserMapper {

    /**
     * Maps a {@link User} entity to a {@link UserResponse} DTO.
     *
     * <p>Password is never copied to the DTO.
     *
     * @param user the source entity
     * @return a safe, password-free response object
     */
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
