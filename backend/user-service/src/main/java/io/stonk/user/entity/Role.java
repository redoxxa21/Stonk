package io.stonk.user.entity;

/**
 * Represents a user's role in the system.
 *
 * <p>Roles are assigned by the Auth Service at registration time.
 * The User Service can read but will not mutate this field.
 */
public enum Role {
    USER,
    ADMIN
}