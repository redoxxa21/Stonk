package io.stonk.auth.kafka;

/**
 * Outbound Kafka topics from auth-service consumed by audit-log-service for debugging.
 */
public final class AuthDomainTopics {

    public static final String USER_REGISTRATION = "user-registration";

    private AuthDomainTopics() {}
}
