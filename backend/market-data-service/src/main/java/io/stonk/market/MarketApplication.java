package io.stonk.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Market Data Service — provides real-time simulated stock prices.
 *
 * <p>On startup, seeds 10 well-known stock symbols with initial prices.
 * A scheduled task updates all prices every {@code market.price-update-interval-ms}
 * milliseconds (default 30 s) with a configurable random fluctuation.
 */
@SpringBootApplication
@EnableScheduling
public class MarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketApplication.class, args);
    }
}
