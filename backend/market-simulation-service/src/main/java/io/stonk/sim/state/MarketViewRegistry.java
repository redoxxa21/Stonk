package io.stonk.sim.state;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MarketViewRegistry {

    private final Map<String, MarketTick> ticks = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> prevForMomentum = new ConcurrentHashMap<>();

   public void seed(String symbol, BigDecimal referencePrice) {
        ticks.putIfAbsent(symbol.toUpperCase(), new MarketTick(referencePrice, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1000)));
        prevForMomentum.putIfAbsent(symbol.toUpperCase(), referencePrice);
    }

    public void updateFromEvent(String symbol, MarketTick tick) {
        String s = symbol.toUpperCase();
        MarketTick old = ticks.get(s);
        if (old != null) {
            prevForMomentum.put(s, old.getLastPrice());
        }
        ticks.put(s, tick);
    }

    public MarketTick get(String symbol) {
        return ticks.get(symbol.toUpperCase());
    }

    public BigDecimal momentumPrev(String symbol) {
        return prevForMomentum.getOrDefault(symbol.toUpperCase(), BigDecimal.ZERO);
    }

    public Set<String> symbols() {
        return ticks.keySet();
    }
}
