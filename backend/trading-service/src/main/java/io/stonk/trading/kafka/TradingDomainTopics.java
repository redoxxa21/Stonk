package io.stonk.trading.kafka;

/**
 * Outbound Kafka topics from trading-service consumed by audit-log-service (and wallet saga).
 */
public final class TradingDomainTopics {

    public static final String TRADE_INITIATED = "trade-initiated";
    /** Published when a BUY/SELL saga completes; audit-log-service persists it for debugging. */
    public static final String TRADE_COMPLETED = "trade-completed";

    private TradingDomainTopics() {}
}
