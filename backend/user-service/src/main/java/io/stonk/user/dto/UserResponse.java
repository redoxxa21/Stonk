package io.stonk.user.dto;

import io.stonk.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

/**
 * Outbound DTO representing a user profile.
 *
 * <p><strong>Password is intentionally absent.</strong> This DTO is the only
 * representation ever sent to API consumers — it acts as a hard boundary
 * that prevents password leakage regardless of what the entity contains.
 */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
}
