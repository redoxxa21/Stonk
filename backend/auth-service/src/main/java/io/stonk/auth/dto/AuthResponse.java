package io.stonk.auth.dto;

import io.stonk.auth.entity.Role;
import lombok.Builder;
import lombok.Getter;

/**
 * Outbound payload returned by both {@code /auth/register} and {@code /auth/login}.
 */
@Getter
@Builder
public class AuthResponse {

    private String token;
    private Long userId;
    private String username;
    private Role role;
    private String message;
}
