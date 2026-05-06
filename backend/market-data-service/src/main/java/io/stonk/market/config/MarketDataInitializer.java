package io.stonk.market.config;

import io.stonk.market.entity.Stock;
import io.stonk.market.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the database with 10 well-known stock symbols on first startup.
 * Stocks that already exist in the database are skipped.
 */
@Slf4j
@Component
public class MarketDataInitializer implements CommandLineRunner {

    private final StockRepository stockRepository;

    public MarketDataInitializer(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void run(String... args) {
        List<Stock> seeds = List.of(
            stock("AAPL",  "Apple Inc.",            189.30),
            stock("GOOGL", "Alphabet Inc.",          175.84),
            stock("MSFT",  "Microsoft Corporation", 415.20),
            stock("AMZN",  "Amazon.com Inc.",        182.50),
            stock("TSLA",  "Tesla Inc.",             177.90),
            stock("META",  "Meta Platforms Inc.",    490.00),
            stock("NVDA",  "NVIDIA Corporation",     875.40),
            stock("NFLX",  "Netflix Inc.",           628.70),
            stock("AMD",   "Advanced Micro Devices", 162.30),
            stock("INTC",  "Intel Corporation",       31.45)
        );

        int seeded = 0;
        for (Stock s : seeds) {
            if (!stockRepository.existsById(s.getSymbol())) {
                stockRepository.save(s);
                seeded++;
            }
        }
        if (seeded > 0) {
            log.info("Seeded {} stock(s) into market_db", seeded);
        } else {
            log.info("All stocks already present — skipping seed");
        }
    }

    private Stock stock(String symbol, String name, double price) {
        BigDecimal p = BigDecimal.valueOf(price);
        return Stock.builder()
                .symbol(symbol)
                .name(name)
                .currentPrice(p)
                .previousClose(p)
                .changePercent(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
