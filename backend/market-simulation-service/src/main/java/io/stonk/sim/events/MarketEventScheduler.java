package io.stonk.sim.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketEventScheduler {

    private final SimulationConditions conditions;

    @Scheduled(fixedDelayString = "${simulation.event-interval-ms:45000}")
    public void randomEvent() {
        conditions.decayIfExpired();
        int r = ThreadLocalRandom.current().nextInt(7);
        MarketEventType type = switch (r) {
            case 0 -> MarketEventType.EARNINGS_BEAT;
            case 1 -> MarketEventType.HYPE_SOCIAL;
            case 2 -> MarketEventType.RECESSION_FEAR;
            case 3 -> MarketEventType.SCANDAL;
            case 4 -> MarketEventType.PRODUCT_LAUNCH;
            case 5 -> MarketEventType.SHORT_SQUEEZE_ALERT;
            default -> MarketEventType.NONE;
        };
        if (type == MarketEventType.NONE) {
            return;
        }
        double hype = switch (type) {
            case HYPE_SOCIAL, PRODUCT_LAUNCH, EARNINGS_BEAT -> 1.4 + ThreadLocalRandom.current().nextDouble(0.6);
            case SHORT_SQUEEZE_ALERT -> 2.0;
            default -> 1.0;
        };
        double fear = switch (type) {
            case RECESSION_FEAR, SCANDAL -> 1.5 + ThreadLocalRandom.current().nextDouble(0.7);
            default -> 1.0;
        };
        double liq = switch (type) {
            case RECESSION_FEAR -> 0.6;
            case HYPE_SOCIAL -> 1.2;
            default -> 1.0;
        };
        int duration = 25 + ThreadLocalRandom.current().nextInt(50);
        conditions.applyEvent(type, duration, hype, fear, liq);
        log.info("Market simulation event {} ({}s)", type, duration);
    }
}
