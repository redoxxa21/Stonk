package io.stonk.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Inbound DTO for updating a user's mutable profile fields.
 *
 * <p>Only {@code username} and {@code email} may be changed through this
 * service. Role and password updates are handled exclusively by the
 * Auth Service to maintain a clear security boundary.
 *
 * <p>Both fields are optional — supply only the field(s) you want to change.
 */
@Getter
@NoArgsConstructor
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
}
