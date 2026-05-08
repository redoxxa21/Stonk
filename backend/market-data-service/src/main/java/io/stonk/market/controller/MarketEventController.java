package io.stonk.market.controller;

import io.stonk.market.dto.ws.LiveCandleMessage;
import io.stonk.market.dto.ws.LiveOrderBookMessage;
import io.stonk.market.dto.ws.MarketEventMessage;
import io.stonk.market.dto.ws.MarketOverviewMessage;
import io.stonk.market.dto.ws.StockSnapshot;
import io.stonk.market.entity.Stock;
import io.stonk.market.exception.StockNotFoundException;
import io.stonk.market.repository.StockRepository;
import io.stonk.market.service.MarketEventBuffer;
import io.stonk.market.service.OrderBookSimulator;
import io.stonk.market.sim.CandleAggregator;
import io.stonk.market.sim.MinuteCandle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * REST endpoints for market events, candles, order book, and overview.
 *
 * <p>These complement the WebSocket topics by providing on-demand
 * snapshot retrieval for clients that missed a broadcast.
 */
@Slf4j
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketEventController {

    private final MarketEventBuffer eventBuffer;
    private final OrderBookSimulator orderBookSimulator;
    private final CandleAggregator candleAggregator;
    private final StockRepository stockRepository;

    // ── Market Events ──────────────────────────────────────────

    /**
     * GET /market/events — returns recent market events (most recent first).
     *
     * @param limit optional, defaults to 20, max 100
     */
    @GetMapping("/events")
    public ResponseEntity<List<MarketEventMessage>> getEvents(
            @RequestParam(defaultValue = "20") int limit) {
        int capped = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(eventBuffer.getRecent(capped));
    }

    // ── Candles ────────────────────────────────────────────────

    /**
     * GET /market/stocks/{symbol}/candles — returns the current 1-minute candle.
     */
    @GetMapping("/stocks/{symbol}/candles")
    public ResponseEntity<LiveCandleMessage> getCandles(@PathVariable String symbol) {
        String sym = symbol.toUpperCase();
        if (!stockRepository.existsById(sym)) {
            throw new StockNotFoundException(sym);
        }

        MinuteCandle candle = candleAggregator.currentCandle(sym);
        if (candle == null) {
            return ResponseEntity.ok(LiveCandleMessage.builder()
                    .symbol(sym)
                    .timeframe("1m")
                    .build());
        }

        return ResponseEntity.ok(LiveCandleMessage.builder()
                .symbol(sym)
                .timeframe("1m")
                .open(candle.getOpen())
                .high(candle.getHigh())
                .low(candle.getLow())
                .close(candle.getClose())
                .volume(candle.getVolume())
                .minuteEpoch(candle.getMinuteEpoch())
                .build());
    }

    // ── Order Book ─────────────────────────────────────────────

    /**
     * GET /market/stocks/{symbol}/orderbook — returns simulated order book snapshot.
     */
    @GetMapping("/stocks/{symbol}/orderbook")
    public ResponseEntity<LiveOrderBookMessage> getOrderBook(@PathVariable String symbol) {
        String sym = symbol.toUpperCase();
        if (!stockRepository.existsById(sym)) {
            throw new StockNotFoundException(sym);
        }
        return ResponseEntity.ok(orderBookSimulator.snapshot(sym));
    }

    // ── Market Overview ────────────────────────────────────────

    /**
     * GET /market/overview — returns top gainers/losers and market status.
     */
    @GetMapping("/overview")
    public ResponseEntity<MarketOverviewMessage> getOverview() {
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

        return ResponseEntity.ok(MarketOverviewMessage.builder()
                .topGainers(gainers)
                .topLosers(losers)
                .marketStatus("OPEN")
                .timestamp(Instant.now().getEpochSecond())
                .build());
    }

    private StockSnapshot toSnapshot(Stock stock) {
        return StockSnapshot.builder()
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .price(stock.getCurrentPrice())
                .changePercent(stock.getChangePercent())
                .build();
    }
}
