package io.stonk.user.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Core User entity — mirrors the Auth Service's User table so both services
 * operate on the same {@code users} schema without duplicating data.
 *
 * <p>Password is stored but MUST NEVER be exposed in any API response or log.
 * Use {@link io.stonk.user.dto.UserResponse} for all outbound representations.
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
