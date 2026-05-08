package io.stonk.market.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.market.dto.TradeExecutedEvent;
import io.stonk.market.service.TradeExecutionProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeExecutedConsumer {

    private final ObjectMapper objectMapper;
    private final TradeExecutionProcessor processor;

    @KafkaListener(
            topics = MarketKafkaTopics.TRADE_EXECUTED,
            groupId = "${spring.application.name}-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTrade(String json) {
        try {
            TradeExecutedEvent evt = objectMapper.readValue(json, TradeExecutedEvent.class);
            processor.applyExecution(evt);
        } catch (Exception ex) {
            log.error("trade-executed handling failed: {}", ex.getMessage());
        }
    }
}
