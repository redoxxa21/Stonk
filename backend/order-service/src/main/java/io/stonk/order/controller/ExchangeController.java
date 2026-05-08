package io.stonk.order.controller;

import io.stonk.order.dto.OrderRequestMessage;
import io.stonk.order.dto.OrderType;
import io.stonk.order.dto.Side;
import io.stonk.order.dto.TradeExecutedEvent;
import io.stonk.order.exchange.ExchangeMatchingEngine;
import io.stonk.order.exchange.OrderBook;
import io.stonk.order.kafka.ExchangeKafkaTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Operator/test API. Primary ingress for bots is Kafka {@code order-request}.
 */
@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeMatchingEngine engine;
    private final org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @GetMapping("/books/{symbol}")
    public ResponseEntity<BookSnapshot> book(@PathVariable String symbol) {
        OrderBook book = engine.getRegistry().book(symbol);
        synchronized (book) {
            BigDecimal bid = book.bestBidPrice();
            BigDecimal ask = book.bestAskPrice();
            return ResponseEntity.ok(new BookSnapshot(
                    symbol.toUpperCase(),
                    bid,
                    ask,
                    book.bestBidSize(),
                    book.bestAskSize()
            ));
        }
    }

    /** Submit order via HTTP (tests); same path as Kafka handler — emits {@code trade-executed} per fill. */
    @PostMapping("/orders")
    public ResponseEntity<List<TradeExecutedEvent>> submitHttp(@Valid @RequestBody HttpOrderRequest body) throws Exception {
        OrderRequestMessage req = OrderRequestMessage.builder()
                .requestId(body.getRequestId())
                .clientId(body.getClientId())
                .symbol(body.getSymbol())
                .side(body.getSide())
                .orderType(body.getOrderType())
                .quantity(body.getQuantity())
                .limitPrice(body.getLimitPrice())
                .build();
        List<TradeExecutedEvent> trades = engine.submit(req);
        for (TradeExecutedEvent t : trades) {
            kafkaTemplate.send(ExchangeKafkaTopics.TRADE_EXECUTED, t.getSymbol(),
                    objectMapper.writeValueAsString(t));
        }
        return ResponseEntity.ok(trades);
    }

    @Data
    public static class BookSnapshot {
        private final String symbol;
        private final BigDecimal bestBid;
        private final BigDecimal bestAsk;
        private final int bidSize;
        private final int askSize;
    }

    @Data
    public static class HttpOrderRequest {
        private String requestId;
        @NotBlank
        private String clientId;
        @NotBlank
        private String symbol;
        @NotNull
        private Side side;
        @NotNull
        private OrderType orderType;
        @NotNull
        @Min(1)
        private Integer quantity;
        private BigDecimal limitPrice;
    }
}
