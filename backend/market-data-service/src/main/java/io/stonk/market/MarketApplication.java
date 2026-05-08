package io.stonk.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Market Data Service — provides real-time simulated stock prices.
 *
 * <p>On startup, seeds 10 well-known stock symbols with reference prices.
 * Prices move only when the exchange emits {@code trade-executed} events.
 */
@SpringBootApplication
@EnableKafka
public class MarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketApplication.class, args);
    }
}
