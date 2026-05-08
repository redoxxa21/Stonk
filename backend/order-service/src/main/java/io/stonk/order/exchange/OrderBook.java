package io.stonk.order.exchange;

import io.stonk.order.dto.OrderRequestMessage;
import io.stonk.order.dto.OrderType;
import io.stonk.order.dto.Side;
import io.stonk.order.dto.TradeExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Price-time priority matching per symbol.
 *
 * <p>Bids: best (highest) price first; within level FIFO ({@link BookOrder#sequence}).
 * Asks: best (lowest) price first; within level FIFO.
 */
@Slf4j
@RequiredArgsConstructor
public class OrderBook {

    private final String symbol;
    /** BUY book: descending price key order. */
    private final NavigableMap<BigDecimal, List<BookOrder>> bids =
            new TreeMap<>(Comparator.reverseOrder());
    /** SELL book: ascending price key order. */
    private final NavigableMap<BigDecimal, List<BookOrder>> asks = new TreeMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public synchronized List<TradeExecutedEvent> handle(OrderRequestMessage req) {
        List<TradeExecutedEvent> trades = new ArrayList<>();
        if (req.getQuantity() <= 0) {
            return trades;
        }
        if (req.getOrderType() == OrderType.LIMIT
                && (req.getLimitPrice() == null || req.getLimitPrice().compareTo(BigDecimal.valueOf(0.01)) < 0)) {
            log.warn("Invalid LIMIT for {} — missing/invalid price", symbol);
            return trades;
        }
        if (req.getOrderType() == OrderType.MARKET) {
            if (req.getSide() == Side.BUY) {
                matchMarketBuy(req, trades);
            } else {
                matchMarketSell(req, trades);
            }
        } else {
            if (req.getSide() == Side.BUY) {
                matchLimitBuy(req, trades);
            } else {
                matchLimitSell(req, trades);
            }
        }
        return trades;
    }

    public synchronized BigDecimal bestBidPrice() {
        return bids.isEmpty() ? null : bids.firstKey();
    }

    public synchronized BigDecimal bestAskPrice() {
        return asks.isEmpty() ? null : asks.firstKey();
    }

    public synchronized int bestBidSize() {
        if (bids.isEmpty()) return 0;
        List<BookOrder> level = bids.firstEntry().getValue();
        return level.stream().mapToInt(BookOrder::getRemainingQty).sum();
    }

    public synchronized int bestAskSize() {
        if (asks.isEmpty()) return 0;
        List<BookOrder> level = asks.firstEntry().getValue();
        return level.stream().mapToInt(BookOrder::getRemainingQty).sum();
    }

    private void matchMarketBuy(OrderRequestMessage incoming, List<TradeExecutedEvent> out) {
        String aggressorOrderId = incoming.getRequestId() != null ? incoming.getRequestId() : UUID.randomUUID().toString();
        int remaining = incoming.getQuantity();
        while (remaining > 0 && !asks.isEmpty()) {
            Map.Entry<BigDecimal, List<BookOrder>> level = asks.firstEntry();
            List<BookOrder> queue = level.getValue();
            if (queue.isEmpty()) {
                asks.remove(level.getKey());
                continue;
            }
            BookOrder resting = queue.get(0);
            int take = Math.min(remaining, resting.getRemainingQty());
            out.add(buildTrade(resting.getPrice(), take, Side.BUY, aggressorOrderId, incoming.getClientId(), resting));
            remaining -= take;
            resting.reduce(take);
            if (resting.isFilled()) {
                queue.remove(0);
                if (queue.isEmpty()) {
                    asks.remove(level.getKey());
                }
            }
        }
        if (remaining > 0) {
            log.debug("MARKET BUY IOC leftover not filled: symbol={} qty={} client={}", symbol, remaining, incoming.getClientId());
       }
    }

    private void matchMarketSell(OrderRequestMessage incoming, List<TradeExecutedEvent> out) {
        String aggressorOrderId = incoming.getRequestId() != null ? incoming.getRequestId() : UUID.randomUUID().toString();
        int remaining = incoming.getQuantity();
        while (remaining > 0 && !bids.isEmpty()) {
            Map.Entry<BigDecimal, List<BookOrder>> level = bids.firstEntry();
            List<BookOrder> queue = level.getValue();
            if (queue.isEmpty()) {
                bids.remove(level.getKey());
                continue;
            }
            BookOrder resting = queue.get(0);
            int take = Math.min(remaining, resting.getRemainingQty());
            out.add(buildTrade(resting.getPrice(), take, Side.SELL, aggressorOrderId, incoming.getClientId(), resting));
            remaining -= take;
            resting.reduce(take);
            if (resting.isFilled()) {
                queue.remove(0);
                if (queue.isEmpty()) {
                    bids.remove(level.getKey());
                }
            }
        }
        if (remaining > 0) {
            log.debug("MARKET SELL IOC leftover not filled: symbol={} qty={} client={}", symbol, remaining, incoming.getClientId());
        }
    }

    private void matchLimitBuy(OrderRequestMessage incoming, List<TradeExecutedEvent> out) {
        BigDecimal limit = incoming.getLimitPrice();
        int remaining = incoming.getQuantity();
        String clientId = incoming.getClientId();
        String orderId = incoming.getRequestId() != null ? incoming.getRequestId() : UUID.randomUUID().toString();
        while (remaining > 0 && !asks.isEmpty()) {
            Map.Entry<BigDecimal, List<BookOrder>> level = asks.firstEntry();
            BigDecimal askPrice = level.getKey();
            if (askPrice.compareTo(limit) > 0) {
                break;
            }
            List<BookOrder> queue = level.getValue();
            if (queue.isEmpty()) {
                asks.remove(askPrice);
                continue;
            }
            BookOrder resting = queue.get(0);
            int take = Math.min(remaining, resting.getRemainingQty());
            out.add(buildTrade(resting.getPrice(), take, Side.BUY, orderId, clientId, resting));
            remaining -= take;
            resting.reduce(take);
            if (resting.isFilled()) {
                queue.remove(0);
                if (queue.isEmpty()) {
                    asks.remove(askPrice);
                }
            }
        }
        if (remaining > 0) {
            long seq = sequence.incrementAndGet();
            BookOrder self = new BookOrder(orderId, clientId, Side.BUY, limit, remaining, seq);
            bids.computeIfAbsent(limit, k -> new ArrayList<>()).add(self);
        }
    }

    private void matchLimitSell(OrderRequestMessage incoming, List<TradeExecutedEvent> out) {
        BigDecimal limit = incoming.getLimitPrice();
        int remaining = incoming.getQuantity();
        String clientId = incoming.getClientId();
        String orderId = incoming.getRequestId() != null ? incoming.getRequestId() : UUID.randomUUID().toString();
        while (remaining > 0 && !bids.isEmpty()) {
            Map.Entry<BigDecimal, List<BookOrder>> level = bids.firstEntry();
            BigDecimal bidPrice = level.getKey();
            if (bidPrice.compareTo(limit) < 0) {
                break;
            }
            List<BookOrder> queue = level.getValue();
            if (queue.isEmpty()) {
                bids.remove(bidPrice);
                continue;
            }
            BookOrder resting = queue.get(0);
            int take = Math.min(remaining, resting.getRemainingQty());
            out.add(buildTrade(resting.getPrice(), take, Side.SELL, orderId, clientId, resting));
            remaining -= take;
            resting.reduce(take);
            if (resting.isFilled()) {
                queue.remove(0);
                if (queue.isEmpty()) {
                    bids.remove(bidPrice);
                }
            }
        }
        if (remaining > 0) {
            long seq = sequence.incrementAndGet();
            BookOrder self = new BookOrder(orderId, clientId, Side.SELL, limit, remaining, seq);
            asks.computeIfAbsent(limit, k -> new ArrayList<>()).add(self);
        }
    }

    private TradeExecutedEvent buildTrade(BigDecimal price, int qty, Side aggressorSide,
                                          String aggressorOrderId, String aggressorClient, BookOrder passive) {
        boolean buyerAggressive = aggressorSide == Side.BUY;
        String buyOid;
        String sellOid;
        String buyClient;
        String sellClient;
        if (buyerAggressive) {
            buyOid = aggressorOrderId;
            sellOid = passive.getOrderId();
            buyClient = aggressorClient;
            sellClient = passive.getClientId();
        } else {
            buyOid = passive.getOrderId();
            sellOid = aggressorOrderId;
            buyClient = passive.getClientId();
            sellClient = aggressorClient;
        }
        return TradeExecutedEvent.builder()
                .tradeId(UUID.randomUUID().toString())
                .symbol(symbol)
                .price(price)
                .quantity(qty)
                .aggressorSide(aggressorSide)
                .buyOrderId(buyOid)
                .sellOrderId(sellOid)
                .buyClientId(buyClient)
                .sellClientId(sellClient)
                .executedAt(Instant.now())
                .build();
    }

    /** Persisting engine: symbol -> book. */
    public static class Registry {
        private final Map<String, OrderBook> books = new ConcurrentHashMap<>();

        public OrderBook book(String symbol) {
            String s = symbol.toUpperCase();
            return books.computeIfAbsent(s, OrderBook::new);
        }
    }
}
