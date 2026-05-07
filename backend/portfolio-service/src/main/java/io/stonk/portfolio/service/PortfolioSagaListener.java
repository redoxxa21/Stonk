package io.stonk.portfolio.service;

import io.stonk.portfolio.entity.TradeType;
import io.stonk.portfolio.event.*;
import io.stonk.portfolio.exception.InsufficientHoldingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioSagaListener {

    private final PortfolioService portfolioService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "trade-initiated", groupId = "${spring.application.name}-group")
    public void onTradeInitiated(TradeInitiatedEvent event) {
        if (event.getType() == TradeType.SELL) {
            log.info("Received TradeInitiatedEvent (SELL) for tradeId: {}", event.getTradeId());
            try {
                portfolioService.reduceHoldingSaga(event.getUserId(), event.getSymbol(), event.getQuantity(), event.getPrice());
                log.info("Successfully reduced portfolio for tradeId: {}", event.getTradeId());
                
                PortfolioDeductedEvent successEvent = PortfolioDeductedEvent.builder()
                        .tradeId(event.getTradeId())
                        .userId(event.getUserId())
                        .symbol(event.getSymbol())
                        .quantity(event.getQuantity())
                        .price(event.getPrice())
                        .build();
                kafkaTemplate.send("portfolio-deducted", successEvent);
                
            } catch (InsufficientHoldingException ex) {
                log.error("Insufficient shares for tradeId: {}", event.getTradeId());
                PortfolioFailedEvent failedEvent = PortfolioFailedEvent.builder()
                        .tradeId(event.getTradeId())
                        .userId(event.getUserId())
                        .symbol(event.getSymbol())
                        .reason("Insufficient shares")
                        .build();
                kafkaTemplate.send("portfolio-failed", failedEvent);
            } catch (Exception ex) {
                log.error("Portfolio reduction failed for tradeId: {}: {}", event.getTradeId(), ex.getMessage());
                PortfolioFailedEvent failedEvent = PortfolioFailedEvent.builder()
                        .tradeId(event.getTradeId())
                        .userId(event.getUserId())
                        .symbol(event.getSymbol())
                        .reason("Unexpected error: " + ex.getMessage())
                        .build();
                kafkaTemplate.send("portfolio-failed", failedEvent);
            }
        }
    }

    @KafkaListener(topics = "portfolio-add-requested", groupId = "${spring.application.name}-group")
    public void onPortfolioAddRequested(PortfolioAddRequestedEvent event) {
        log.info("Received PortfolioAddRequestedEvent for tradeId: {}", event.getTradeId());
        try {
            portfolioService.addHoldingSaga(event.getUserId(), event.getSymbol(), event.getQuantity(), event.getPrice());
            log.info("Successfully added to portfolio for tradeId: {}", event.getTradeId());
            
            PortfolioAddedEvent successEvent = PortfolioAddedEvent.builder()
                    .tradeId(event.getTradeId())
                    .userId(event.getUserId())
                    .symbol(event.getSymbol())
                    .quantity(event.getQuantity())
                    .price(event.getPrice())
                    .build();
            kafkaTemplate.send("portfolio-added", successEvent);
            
        } catch (Exception ex) {
            log.error("Portfolio addition failed for tradeId: {}: {}", event.getTradeId(), ex.getMessage());
            PortfolioFailedEvent failedEvent = PortfolioFailedEvent.builder()
                    .tradeId(event.getTradeId())
                    .userId(event.getUserId())
                    .symbol(event.getSymbol())
                    .reason("Unexpected error: " + ex.getMessage())
                    .build();
            kafkaTemplate.send("portfolio-failed", failedEvent);
        }
    }
}
