package io.stonk.market.websocket;

import io.stonk.market.dto.ws.*;
import io.stonk.market.entity.Stock;
import io.stonk.market.repository.StockRepository;
import io.stonk.market.service.MarketBroadcastService;
import io.stonk.market.sim.CandleAggregator;
import io.stonk.market.sim.MinuteCandle;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the WebSocket layer.
 *
 * <p>Uses an embedded H2 database and real STOMP client to verify
 * handshake, subscriptions, and broadcast delivery.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MarketBroadcastService broadcastService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CandleAggregator candleAggregator;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterEach
    void teardown() {
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    // ── Helper ─────────────────────────────────────────────────

    private StompSession connectSync() throws Exception {
        String url = "ws://localhost:" + port + "/ws-market";
        return stompClient.connectAsync(url, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
    }

    private Stock ensureStock(String symbol) {
        return stockRepository.findById(symbol).orElseGet(() -> {
            Stock s = Stock.builder()
                    .symbol(symbol)
                    .name("Test " + symbol)
                    .currentPrice(BigDecimal.valueOf(100))
                    .previousClose(BigDecimal.valueOf(100))
                    .changePercent(BigDecimal.ZERO)
                    .lastUpdated(LocalDateTime.now())
                    .cumulativeVolume(0L)
                    .realizedVolatility(BigDecimal.ZERO)
                    .liquidityScore(BigDecimal.valueOf(1000))
                    .build();
            return stockRepository.save(s);
        });
    }

    // ── 1. Connection ──────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("WebSocket handshake succeeds")
    void testConnectionSuccess() throws Exception {
        StompSession session = connectSync();
        assertTrue(session.isConnected(), "STOMP session should be connected");
        session.disconnect();
    }

    // ── 2. Stock Price Topic ───────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("Subscribe to /topic/stocks/{symbol} and receive update")
    void testStockPriceBroadcast() throws Exception {
        Stock stock = ensureStock("AAPL");
        stock.setCurrentPrice(BigDecimal.valueOf(185.50));
        stock.setChangePercent(BigDecimal.valueOf(1.25));
        stock.setCumulativeVolume(5000);
        stockRepository.save(stock);

        StompSession session = connectSync();
        BlockingQueue<LiveStockPriceMessage> queue = new LinkedBlockingQueue<>();

        session.subscribe("/topic/stocks/AAPL", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LiveStockPriceMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((LiveStockPriceMessage) payload);
            }
        });

        Thread.sleep(300); // Allow subscription to register
        broadcastService.broadcastStockPrice("AAPL", stock);

        LiveStockPriceMessage msg = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(msg, "Should receive stock price message");
        assertEquals("AAPL", msg.getSymbol());
        assertEquals(0, BigDecimal.valueOf(185.50).compareTo(msg.getPrice()));
        session.disconnect();
    }

    // ── 3. Candle Topic ────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("Subscribe to /topic/candles/{symbol} and receive update")
    void testCandleBroadcast() throws Exception {
        ensureStock("GOOGL");
        MinuteCandle candle = candleAggregator.touch("GOOGL",
                BigDecimal.valueOf(175.00), 100, Instant.now());

        StompSession session = connectSync();
        BlockingQueue<LiveCandleMessage> queue = new LinkedBlockingQueue<>();

        session.subscribe("/topic/candles/GOOGL", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LiveCandleMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((LiveCandleMessage) payload);
            }
        });

        Thread.sleep(300);
        broadcastService.broadcastCandle("GOOGL", candle);

        LiveCandleMessage msg = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(msg, "Should receive candle message");
        assertEquals("GOOGL", msg.getSymbol());
        assertEquals("1m", msg.getTimeframe());
        session.disconnect();
    }

    // ── 4. Order Book Topic ────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Subscribe to /topic/orderbook/{symbol} and receive update")
    void testOrderBookBroadcast() throws Exception {
        ensureStock("MSFT");

        StompSession session = connectSync();
        BlockingQueue<LiveOrderBookMessage> queue = new LinkedBlockingQueue<>();

        session.subscribe("/topic/orderbook/MSFT", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LiveOrderBookMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((LiveOrderBookMessage) payload);
            }
        });

        Thread.sleep(300);
        broadcastService.broadcastOrderBook("MSFT", BigDecimal.valueOf(415.20), 50);

        LiveOrderBookMessage msg = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(msg, "Should receive order book message");
        assertEquals("MSFT", msg.getSymbol());
        assertFalse(msg.getBids().isEmpty(), "Bids should not be empty");
        assertFalse(msg.getAsks().isEmpty(), "Asks should not be empty");
        assertEquals(5, msg.getBids().size(), "Should have 5 bid levels");
        assertEquals(5, msg.getAsks().size(), "Should have 5 ask levels");
        session.disconnect();
    }

    // ── 5. Market Overview Topic ───────────────────────────────

    @Test
    @Order(5)
    @DisplayName("Subscribe to /topic/market/overview and receive update")
    void testMarketOverviewBroadcast() throws Exception {
        ensureStock("AAPL");

        StompSession session = connectSync();
        BlockingQueue<MarketOverviewMessage> queue = new LinkedBlockingQueue<>();

        session.subscribe("/topic/market/overview", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MarketOverviewMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((MarketOverviewMessage) payload);
            }
        });

        Thread.sleep(300);
        broadcastService.broadcastMarketOverview();

        MarketOverviewMessage msg = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(msg, "Should receive overview message");
        assertEquals("OPEN", msg.getMarketStatus());
        assertNotNull(msg.getTopGainers());
        assertNotNull(msg.getTopLosers());
        session.disconnect();
    }

    // ── 6. Market Events Topic ─────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("Subscribe to /topic/market/events and receive event")
    void testMarketEventBroadcast() throws Exception {
        StompSession session = connectSync();
        BlockingQueue<MarketEventMessage> queue = new LinkedBlockingQueue<>();

        session.subscribe("/topic/market/events", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MarketEventMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((MarketEventMessage) payload);
            }
        });

        Thread.sleep(300);

        MarketEventMessage event = MarketEventMessage.builder()
                .eventType("PANIC_SELLING")
                .symbol("TSLA")
                .severity(0.8)
                .message("High volatility detected")
                .timestamp(Instant.now().getEpochSecond())
                .build();
        broadcastService.broadcastMarketEvent(event);

        MarketEventMessage msg = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(msg, "Should receive market event");
        assertEquals("PANIC_SELLING", msg.getEventType());
        assertEquals("TSLA", msg.getSymbol());
        session.disconnect();
    }

    // ── 7. Multiple Clients ────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("Multiple clients receive the same broadcast")
    void testMultipleClients() throws Exception {
        Stock stock = ensureStock("NVDA");
        stock.setCurrentPrice(BigDecimal.valueOf(900));
        stockRepository.save(stock);

        StompSession session1 = connectSync();
        StompSession session2 = connectSync();

        BlockingQueue<LiveStockPriceMessage> queue1 = new LinkedBlockingQueue<>();
        BlockingQueue<LiveStockPriceMessage> queue2 = new LinkedBlockingQueue<>();

        StompFrameHandler handler1 = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LiveStockPriceMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue1.add((LiveStockPriceMessage) payload);
            }
        };

        StompFrameHandler handler2 = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LiveStockPriceMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue2.add((LiveStockPriceMessage) payload);
            }
        };

        session1.subscribe("/topic/stocks/NVDA", handler1);
        session2.subscribe("/topic/stocks/NVDA", handler2);

        Thread.sleep(300);
        broadcastService.broadcastStockPrice("NVDA", stock);

        LiveStockPriceMessage msg1 = queue1.poll(5, TimeUnit.SECONDS);
        LiveStockPriceMessage msg2 = queue2.poll(5, TimeUnit.SECONDS);

        assertNotNull(msg1, "Client 1 should receive message");
        assertNotNull(msg2, "Client 2 should receive message");
        assertEquals("NVDA", msg1.getSymbol());
        assertEquals("NVDA", msg2.getSymbol());

        session1.disconnect();
        session2.disconnect();
    }
}
