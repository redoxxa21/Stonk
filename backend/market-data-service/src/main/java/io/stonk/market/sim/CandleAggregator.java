package io.stonk.market.sim;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CandleAggregator {

    private final Map<String, MinuteCandle> current = new ConcurrentHashMap<>();

    public MinuteCandle touch(String symbol, BigDecimal price, int quantity, Instant executedAt) {
        long minute = executedAt.getEpochSecond() / 60;
        return current.compute(symbol, (sym, existing) -> {
            if (existing == null || existing.getMinuteEpoch() != minute) {
                return new MinuteCandle(minute, price, quantity);
            }
            existing.add(price, quantity);
            return existing;
        });
    }

    /** Returns the current candle for the given symbol, or null if none exists. */
    public MinuteCandle currentCandle(String symbol) {
        return current.get(symbol);
    }
}
