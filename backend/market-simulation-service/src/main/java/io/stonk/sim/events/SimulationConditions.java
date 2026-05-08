package io.stonk.sim.events;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Global mood adjusted by scheduled {@link MarketEventScheduler} — bots read-only.
 */
@Component
public class SimulationConditions {

    private volatile BigDecimal hypeMultiplier = BigDecimal.ONE;
    private volatile BigDecimal fearMultiplier = BigDecimal.ONE;
    private volatile BigDecimal liquidityBias = BigDecimal.ONE;
    private volatile MarketEventType activeEvent = MarketEventType.NONE;
    private volatile Instant eventUntil = Instant.EPOCH;

    public void applyEvent(MarketEventType type, int durationSec, double hype, double fear, double liq) {
        this.activeEvent = type;
        this.eventUntil = Instant.now().plusSeconds(durationSec);
        this.hypeMultiplier = BigDecimal.valueOf(hype);
        this.fearMultiplier = BigDecimal.valueOf(fear);
        this.liquidityBias = BigDecimal.valueOf(liq);
    }

    public void decayIfExpired() {
        if (Instant.now().isAfter(eventUntil)) {
            hypeMultiplier = BigDecimal.ONE;
            fearMultiplier = BigDecimal.ONE;
            liquidityBias = BigDecimal.ONE;
            activeEvent = MarketEventType.NONE;
        }
    }

    public BigDecimal hypeMultiplier() { return hypeMultiplier; }
    public BigDecimal fearMultiplier() { return fearMultiplier; }
    public BigDecimal liquidityBias() { return liquidityBias; }
    public MarketEventType activeEvent() { return activeEvent; }
}
