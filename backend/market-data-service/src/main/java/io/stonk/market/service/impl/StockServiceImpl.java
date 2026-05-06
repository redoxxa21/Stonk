package io.stonk.market.service.impl;

import io.stonk.market.dto.StockResponse;
import io.stonk.market.entity.Stock;
import io.stonk.market.exception.StockNotFoundException;
import io.stonk.market.repository.StockRepository;
import io.stonk.market.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final double maxFluctuationPercent;
    private final Random random = new Random();

    public StockServiceImpl(StockRepository stockRepository,
                            @Value("${market.max-fluctuation-percent:2.0}") double maxFluctuationPercent) {
        this.stockRepository = stockRepository;
        this.maxFluctuationPercent = maxFluctuationPercent;
    }

    @Override
    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findById(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException(symbol));
        return toResponse(stock);
    }

    @Override
    @Transactional
    public void updateAllPrices() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            BigDecimal oldPrice = stock.getCurrentPrice();
            BigDecimal newPrice = applyFluctuation(oldPrice);
            BigDecimal changePercent = newPrice.subtract(stock.getPreviousClose())
                    .divide(stock.getPreviousClose(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(4, RoundingMode.HALF_UP);

            stock.setCurrentPrice(newPrice);
            stock.setChangePercent(changePercent);
            stock.setLastUpdated(LocalDateTime.now());
        }
        stockRepository.saveAll(stocks);
        log.debug("Updated prices for {} stocks", stocks.size());
    }

    // ── Helpers ──────────────────────────────────────

    private BigDecimal applyFluctuation(BigDecimal price) {
        double fluctuation = (random.nextDouble() * 2 - 1) * maxFluctuationPercent / 100.0;
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(fluctuation));
        return price.multiply(factor).setScale(4, RoundingMode.HALF_UP)
                .max(BigDecimal.valueOf(0.01)); // never go below 1 cent
    }

    private StockResponse toResponse(Stock stock) {
        return StockResponse.builder()
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .currentPrice(stock.getCurrentPrice())
                .previousClose(stock.getPreviousClose())
                .changePercent(stock.getChangePercent())
                .lastUpdated(stock.getLastUpdated())
                .build();
    }
}
