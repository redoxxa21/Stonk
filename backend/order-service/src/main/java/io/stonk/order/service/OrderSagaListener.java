package io.stonk.order.service;

import io.stonk.order.entity.OrderStatus;
import io.stonk.order.entity.TradeOrder;
import io.stonk.order.event.TradeCompletedEvent;
import io.stonk.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "trade-completed", groupId = "${spring.application.name}-group")
    @Transactional
    public void onTradeCompleted(TradeCompletedEvent event) {
        log.info("[Saga] Received TradeCompletedEvent for tradeId: {}", event.getTradeId());
        
        BigDecimal total = event.getPrice().multiply(BigDecimal.valueOf(event.getQuantity()));
        
        TradeOrder order = TradeOrder.builder()
                .userId(event.getUserId())
                .symbol(event.getSymbol().toUpperCase())
                .type(io.stonk.order.entity.OrderType.valueOf(event.getType().name())) // Buy/Sell
                .quantity(event.getQuantity())
                .price(event.getPrice())
                .totalAmount(total)
                .status(OrderStatus.COMPLETED) // Instantly marked as COMPLETED because trade finished successfully
                .build();
                
        orderRepository.save(order);
        log.info("[Saga] Created COMPLETED order #{} for tradeId: {}", order.getId(), event.getTradeId());
    }
}
