package io.stonk.market.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarketKafkaTopics {

    public static final String TRADE_EXECUTED = "trade-executed";
    public static final String MARKET_PRICE_UPDATED = "market-price-updated";
}
