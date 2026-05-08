package io.stonk.market.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.market.dto.ws.MarketEventMessage;
import io.stonk.market.service.MarketBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consumes {@code market-event-created} and {@code volatility-changed}
 * Kafka topics and broadcasts them to WebSocket clients.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketEventConsumer {

    private final ObjectMapper objectMapper;
    private final MarketBroadcastService broadcastService;

    @KafkaListener(
            topics = MarketKafkaTopics.MARKET_EVENT_CREATED,
            groupId = "${spring.application.name}-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMarketEvent(String json) {
        try {
            MarketEventMessage event = objectMapper.readValue(json, MarketEventMessage.class);
            if (event.getTimestamp() == 0) {
                event.setTimestamp(Instant.now().getEpochSecond());
            }
            broadcastService.broadcastMarketEvent(event);
            log.info("Broadcast market event: {} for {}", event.getEventType(), event.getSymbol());
        } catch (Exception ex) {
            log.error("market-event-created handling failed: {}", ex.getMessage());
        }
    }

    @KafkaListener(
            topics = MarketKafkaTopics.VOLATILITY_CHANGED,
            groupId = "${spring.application.name}-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onVolatilityChanged(String json) {
        try {
            // Volatility change events trigger a market event broadcast
            MarketEventMessage event = objectMapper.readValue(json, MarketEventMessage.class);
            if (event.getTimestamp() == 0) {
                event.setTimestamp(Instant.now().getEpochSecond());
            }
            if (event.getEventType() == null) {
                event.setEventType("VOLATILITY_CHANGE");
            }
            broadcastService.broadcastMarketEvent(event);
            log.info("Broadcast volatility change: {} severity={}", event.getSymbol(), event.getSeverity());
        } catch (Exception ex) {
            log.error("volatility-changed handling failed: {}", ex.getMessage());
        }
    }
}
