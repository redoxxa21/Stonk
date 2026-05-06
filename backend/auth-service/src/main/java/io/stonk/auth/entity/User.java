package io.stonk.auth.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a registered user's credentials and identity.
 *
 * <p>This entity is the source of truth for authentication data.
 * The User Service mirrors this schema to read profile information
 * without duplicating credential management.
 *
 * <p>Password is always stored as a BCrypt hash — never plaintext.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. Never expose in responses or logs. */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}