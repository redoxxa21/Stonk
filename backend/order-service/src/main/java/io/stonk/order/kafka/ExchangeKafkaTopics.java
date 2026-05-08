package io.stonk.order.kafka;

/** Kafka topic names for the in-memory exchange. */
public final class ExchangeKafkaTopics {

    private ExchangeKafkaTopics() {
        // Constants holder
    }

    public static final String ORDER_REQUEST = "order-request";
    public static final String TRADE_EXECUTED = "trade-executed";
}
