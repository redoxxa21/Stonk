package io.stonk.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.order.dto.OrderRequestMessage;
import io.stonk.order.dto.TradeExecutedEvent;
import io.stonk.order.exchange.ExchangeMatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRequestKafkaListener {

    private final ExchangeMatchingEngine engine;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = ExchangeKafkaTopics.ORDER_REQUEST,
            groupId = "${spring.application.name}-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderRequest(String json) {
        try {
            OrderRequestMessage req = objectMapper.readValue(json, OrderRequestMessage.class);
            List<TradeExecutedEvent> trades = engine.submit(req);
            for (TradeExecutedEvent t : trades) {
                kafkaTemplate.send(ExchangeKafkaTopics.TRADE_EXECUTED, t.getSymbol(),
                        objectMapper.writeValueAsString(t));
            }
            if (!trades.isEmpty()) {
                log.debug("Matched {} trade(s) for {}", trades.size(), req.getSymbol());
            }
        } catch (Exception ex) {
            log.error("Failed order-request: {}", ex.getMessage());
        }
    }
}
