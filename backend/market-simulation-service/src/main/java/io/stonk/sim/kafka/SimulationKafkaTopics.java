package io.stonk.sim.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimulationKafkaTopics {

    public static final String ORDER_REQUEST = "order-request";
    public static final String MARKET_PRICE_UPDATED = "market-price-updated";
}
