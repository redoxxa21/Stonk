package io.stonk.market.scheduler;

import io.stonk.market.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that updates all stock prices at a fixed interval.
 *
 * <p>The interval is controlled by {@code market.price-update-interval-ms}
 * in {@code application.yaml} (default: 30 000 ms = 30 seconds).
 */
@Slf4j
@Component
public class PriceUpdateScheduler {

    private final StockService stockService;

    public PriceUpdateScheduler(StockService stockService) {
        this.stockService = stockService;
    }

    @Scheduled(fixedRateString = "${market.price-update-interval-ms:30000}")
    public void tick() {
        log.info("Price update tick — updating all stock prices");
        stockService.updateAllPrices();
    }
}
