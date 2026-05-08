package io.stonk.market.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.market.dto.MarketPriceUpdatedEvent;
import io.stonk.market.dto.TradeExecutedEvent;
import io.stonk.market.entity.Stock;
import io.stonk.market.kafka.MarketKafkaTopics;
import io.stonk.market.repository.StockRepository;
import io.stonk.market.sim.CandleAggregator;
import io.stonk.market.sim.MinuteCandle;
import io.stonk.market.sim.ReturnVolatilityTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeExecutionProcessor {

    private final StockRepository stockRepository;
    private final CandleAggregator candleAggregator;
    private final ReturnVolatilityTracker volatilityTracker;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void applyExecution(TradeExecutedEvent trade) {
        String sym = trade.getSymbol().toUpperCase();
        Stock stock = stockRepository.findById(sym).orElse(null);
        if (stock == null) {
            log.warn("trade-executed for unknown symbol {} — ignoring", sym);
            return;
        }
        Instant executedAt = trade.getExecutedAt() != null ? trade.getExecutedAt() : Instant.now();

        BigDecimal oldPrice = stock.getCurrentPrice();
        volatilityTracker.recordPriceChange(sym, oldPrice, trade.getPrice());

        stock.setCurrentPrice(trade.getPrice());
        stock.setCumulativeVolume(stock.getCumulativeVolume() + trade.getQuantity());
        stock.setChangePercent(trade.getPrice().subtract(stock.getPreviousClose())
                .divide(stock.getPreviousClose(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP));
        stock.setRealizedVolatility(volatilityTracker.annualizedVolPercent(sym));

        BigDecimal vol = stock.getRealizedVolatility().max(BigDecimal.valueOf(0.0001));
        BigDecimal liquidity = BigDecimal.valueOf(stock.getCumulativeVolume())
                .divide(vol, 6, RoundingMode.HALF_UP);
        stock.setLiquidityScore(liquidity.setScale(4, RoundingMode.HALF_UP));

        stock.setLastUpdated(LocalDateTime.ofInstant(executedAt, ZoneOffset.UTC));

        stockRepository.save(stock);

        MinuteCandle candle = candleAggregator.touch(sym, trade.getPrice(), trade.getQuantity(), executedAt);

        MarketPriceUpdatedEvent evt = MarketPriceUpdatedEvent.builder()
                .symbol(sym)
                .lastPrice(stock.getCurrentPrice())
                .changePercent(stock.getChangePercent())
                .cumulativeVolume(stock.getCumulativeVolume())
                .realizedVolatility(stock.getRealizedVolatility())
                .liquidityScore(stock.getLiquidityScore())
                .candleOpen(candle.getOpen())
                .candleHigh(candle.getHigh())
                .candleLow(candle.getLow())
                .candleClose(candle.getClose())
                .candleVolume(candle.getVolume())
                .candleMinuteEpoch(candle.getMinuteEpoch())
                .publishedAt(Instant.now())
                .build();
        try {
            kafkaTemplate.send(MarketKafkaTopics.MARKET_PRICE_UPDATED, sym, objectMapper.writeValueAsString(evt));
        } catch (Exception e) {
            log.warn("Failed to publish market-price-updated: {}", e.getMessage());
        }
    }
}
