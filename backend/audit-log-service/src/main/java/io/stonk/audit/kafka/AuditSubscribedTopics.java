package io.stonk.audit.kafka;

/**
 * Topic names persisted by {@link AuditKafkaListener}.
 *
 * <p>Producers: auth-service publishes {@link #USER_REGISTRATION}; trading-service publishes
 * {@link #TRADE_INITIATED} and downstream saga topics are emitted by wallet/portfolio/trading.
 */
public final class AuditSubscribedTopics {

    public static final String USER_REGISTRATION = "user-registration";
    public static final String TRADE_INITIATED = "trade-initiated";
    public static final String TRADE_COMPLETED = "trade-completed";

    private AuditSubscribedTopics() {}
}
