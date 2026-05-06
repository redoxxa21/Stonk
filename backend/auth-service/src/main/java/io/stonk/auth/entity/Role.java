package io.stonk.auth.entity;

/**
 * Represents the access level of a registered user.
 *
 * <p>The role is embedded in every JWT token as the {@code role} claim,
 * allowing downstream services (User Service, API Gateway) to make
 * authorisation decisions without a database call.
 */
public enum Role {
    USER,
    ADMIN
}