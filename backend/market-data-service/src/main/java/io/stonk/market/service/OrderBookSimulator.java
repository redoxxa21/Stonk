package io.stonk.market.service;

import io.stonk.market.dto.ws.LiveOrderBookMessage;
import io.stonk.market.dto.ws.OrderBookEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains a simulated order book per symbol.
 *
 * <p>On each trade, synthetic bid/ask levels are placed around the
 * last trade price — 5 levels on each side with spread proportional
 * to a small tick size. Volume decreases away from the mid-price.
 */
@Component
public class OrderBookSimulator {

    private static final int LEVELS = 5;
    private static final BigDecimal TICK = new BigDecimal("0.05");

    /** Stores the latest snapshot per symbol. */
    private final Map<String, LiveOrderBookMessage> books = new ConcurrentHashMap<>();

    /**
     * Regenerates the simulated book for the given symbol based on the
     * last executed trade price and quantity.
     */
    public void updateBook(String symbol, BigDecimal lastPrice, int lastQty) {
        int baseQty = Math.max(lastQty, 50);

        List<OrderBookEntry> bids = new ArrayList<>(LEVELS);
        List<OrderBookEntry> asks = new ArrayList<>(LEVELS);

        for (int i = 1; i <= LEVELS; i++) {
            BigDecimal offset = TICK.multiply(BigDecimal.valueOf(i));

            bids.add(OrderBookEntry.builder()
                    .price(lastPrice.subtract(offset).setScale(4, RoundingMode.HALF_UP))
                    .quantity(baseQty + (LEVELS - i) * 20)
                    .build());

            asks.add(OrderBookEntry.builder()
                    .price(lastPrice.add(offset).setScale(4, RoundingMode.HALF_UP))
                    .quantity(baseQty + (LEVELS - i) * 20)
                    .build());
        }

        books.put(symbol, LiveOrderBookMessage.builder()
                .symbol(symbol)
                .bids(bids)
                .asks(asks)
                .timestamp(Instant.now().getEpochSecond())
                .build());
    }

    /**
     * Returns the latest order book snapshot for the symbol,
     * or an empty book if no trades have occurred yet.
     */
    public LiveOrderBookMessage snapshot(String symbol) {
        return books.getOrDefault(symbol, LiveOrderBookMessage.builder()
                .symbol(symbol)
                .bids(List.of())
                .asks(List.of())
                .timestamp(Instant.now().getEpochSecond())
                .build());
    }
}
