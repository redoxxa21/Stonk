package io.stonk.market.service;

import io.stonk.market.dto.ws.*;
import io.stonk.market.entity.Stock;
import io.stonk.market.repository.StockRepository;
import io.stonk.market.sim.MinuteCandle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Central broadcaster that pushes live market data to all
 * subscribed WebSocket clients via STOMP topics.
 *
 * <p>All methods are <b>non-blocking</b> — {@link SimpMessagingTemplate#convertAndSend}
 * is fire-and-forget, so Kafka consumer threads are never held up.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final StockRepository stockRepository;
    private final OrderBookSimulator orderBookSimulator;
    private final MarketEventBuffer marketEventBuffer;

    // ── Stock Price ────────────────────────────────────────────

    /**
     * Broadcasts a live stock price update to {@code /topic/stocks/{symbol}}.
     */
    public void broadcastStockPrice(String symbol, Stock stock) {
        LiveStockPriceMessage msg = LiveStockPriceMessage.builder()
                .symbol(symbol)
                .price(stock.getCurrentPrice())
                .changePercent(stock.getChangePercent())
                .volume(stock.getCumulativeVolume())
                .realizedVolatility(stock.getRealizedVolatility())
                .liquidityScore(stock.getLiquidityScore())
                .timestamp(Instant.now().getEpochSecond())
                .build();

        messagingTemplate.convertAndSend("/topic/stocks/" + symbol, msg);
        log.debug("Broadcast stock price: {} @ {}", symbol, stock.getCurrentPrice());
    }

    // ── Candle ─────────────────────────────────────────────────

    /**
     * Broadcasts a live candle update to {@code /topic/candles/{symbol}}.
     */
    public void broadcastCandle(String symbol, MinuteCandle candle) {
        LiveCandleMessage msg = LiveCandleMessage.builder()
                .symbol(symbol)
                .timeframe("1m")
                .open(candle.getOpen())
                .high(candle.getHigh())
                .low(candle.getLow())
                .close(candle.getClose())
                .volume(candle.getVolume())
                .minuteEpoch(candle.getMinuteEpoch())
                .build();

        messagingTemplate.convertAndSend("/topic/candles/" + symbol, msg);
        log.debug("Broadcast candle: {} minute={}", symbol, candle.getMinuteEpoch());
    }

    // ── Order Book ─────────────────────────────────────────────

    /**
     * Broadcasts a simulated order book snapshot to {@code /topic/orderbook/{symbol}}.
     */
    public void broadcastOrderBook(String symbol, BigDecimal lastPrice, int lastQty) {
        orderBookSimulator.updateBook(symbol, lastPrice, lastQty);
        LiveOrderBookMessage msg = orderBookSimulator.snapshot(symbol);

        messagingTemplate.convertAndSend("/topic/orderbook/" + symbol, msg);
        log.debug("Broadcast order book: {} ({} bids, {} asks)",
                symbol, msg.getBids().size(), msg.getAsks().size());
    }

    // ── Market Overview ────────────────────────────────────────

    /**
     * Broadcasts a market overview (top gainers/losers) to {@code /topic/market/overview}.
     */
    public void broadcastMarketOverview() {
        List<Stock> allStocks = stockRepository.findAll();

        List<StockSnapshot> gainers = allStocks.stream()
                .sorted(Comparator.comparing(Stock::getChangePercent).reversed())
                .limit(5)
                .map(this::toSnapshot)
                .toList();

        List<StockSnapshot> losers = allStocks.stream()
                .sorted(Comparator.comparing(Stock::getChangePercent))
                .limit(5)
                .map(this::toSnapshot)
                .toList();

        MarketOverviewMessage msg = MarketOverviewMessage.builder()
                .topGainers(gainers)
                .topLosers(losers)
                .marketStatus("OPEN")
                .timestamp(Instant.now().getEpochSecond())
                .build();

        messagingTemplate.convertAndSend("/topic/market/overview", msg);
        log.debug("Broadcast market overview: {} gainers, {} losers", gainers.size(), losers.size());
    }

    // ── Market Events ──────────────────────────────────────────

    /**
     * Broadcasts a market event to {@code /topic/market/events} and buffers it
     * for REST retrieval.
     */
    public void broadcastMarketEvent(MarketEventMessage event) {
        marketEventBuffer.add(event);
        messagingTemplate.convertAndSend("/topic/market/events", event);
        log.debug("Broadcast market event: {} on {}", event.getEventType(), event.getSymbol());
    }

    // ── Helpers ────────────────────────────────────────────────

    private StockSnapshot toSnapshot(Stock stock) {
        return StockSnapshot.builder()
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .price(stock.getCurrentPrice())
                .changePercent(stock.getChangePercent())
                .build();
    }
}
