package io.stonk.auth.dto;

import io.stonk.auth.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
}
