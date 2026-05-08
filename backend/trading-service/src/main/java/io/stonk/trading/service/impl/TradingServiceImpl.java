package io.stonk.trading.service.impl;

import io.stonk.trading.client.MarketDataClient;
import io.stonk.trading.security.JwtUser;

import io.stonk.trading.dto.TradeRequest;
import io.stonk.trading.dto.TradeResponse;

import io.stonk.trading.entity.Trade;
import io.stonk.trading.entity.TradeStatus;
import io.stonk.trading.entity.TradeType;
import io.stonk.trading.kafka.TradingDomainTopics;
import io.stonk.trading.exception.TradeNotFoundException;
import io.stonk.trading.exception.TradeExecutionException;
import io.stonk.trading.repository.TradeRepository;
import io.stonk.trading.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TradingServiceImpl implements TradingService {

    private final TradeRepository tradeRepository;
    private final MarketDataClient marketDataClient;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TradingServiceImpl(TradeRepository tradeRepository,
                              MarketDataClient marketDataClient,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.tradeRepository = tradeRepository;
        this.marketDataClient = marketDataClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * BUY flow (Saga Orchestrator):
     * 1. Get current price from Market Data Service
     * 2. Save Trade as PENDING
     * 3. Publish TradeInitiatedEvent
     */
    @Override
    @Transactional
    public TradeResponse executeBuy(TradeRequest req, String authHeader) {
        String symbol = req.getSymbol().toUpperCase();
        JwtUser user = validateUserAccess(req.getUserId());
        log.info("Initiating BUY Saga: userId={}, symbol={}, qty={}", req.getUserId(), symbol, req.getQuantity());

        try {
            BigDecimal price = marketDataClient.getCurrentPrice(symbol, authHeader);
            BigDecimal totalCost = price.multiply(BigDecimal.valueOf(req.getQuantity()));

            Trade trade = Trade.builder()
                    .userId(user.id()).symbol(symbol).type(TradeType.BUY)
                    .quantity(req.getQuantity()).price(price).totalAmount(totalCost)
                    .status(TradeStatus.PENDING).build();
            trade = tradeRepository.save(trade);

            io.stonk.trading.event.TradeInitiatedEvent event = io.stonk.trading.event.TradeInitiatedEvent.builder()
                    .tradeId(trade.getId())
                    .userId(user.id())
                    .symbol(symbol)
                    .quantity(req.getQuantity())
                    .price(price)
                    .type(TradeType.BUY)
                    .totalAmount(totalCost)
                    .build();

            publishTradeInitiatedAfterCommit(trade.getId(), event);
            log.info("Saga Initiated - BUY trade #{} pending", trade.getId());
            return toResponse(trade);

        } catch (Exception ex) {
            log.error("BUY initiation failed for userId:{} symbol:{} — {}", req.getUserId(), symbol, ex.getMessage());
            throw new TradeExecutionException("Buy trade initiation failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * SELL flow (Saga Orchestrator):
     * 1. Get current price from Market Data Service
     * 2. Save Trade as PENDING
     * 3. Publish TradeInitiatedEvent
     */
    @Override
    @Transactional
    public TradeResponse executeSell(TradeRequest req, String authHeader) {
        String symbol = req.getSymbol().toUpperCase();
        JwtUser user = validateUserAccess(req.getUserId());
        log.info("Initiating SELL Saga: userId={}, symbol={}, qty={}", req.getUserId(), symbol, req.getQuantity());

        try {
            BigDecimal price = marketDataClient.getCurrentPrice(symbol, authHeader);
            BigDecimal totalProceeds = price.multiply(BigDecimal.valueOf(req.getQuantity()));

            Trade trade = Trade.builder()
                    .userId(user.id()).symbol(symbol).type(TradeType.SELL)
                    .quantity(req.getQuantity()).price(price).totalAmount(totalProceeds)
                    .status(TradeStatus.PENDING).build();
            trade = tradeRepository.save(trade);

            io.stonk.trading.event.TradeInitiatedEvent event = io.stonk.trading.event.TradeInitiatedEvent.builder()
                    .tradeId(trade.getId())
                    .userId(user.id())
                    .symbol(symbol)
                    .quantity(req.getQuantity())
                    .price(price)
                    .type(TradeType.SELL)
                    .totalAmount(totalProceeds)
                    .build();

            publishTradeInitiatedAfterCommit(trade.getId(), event);
            log.info("Saga Initiated - SELL trade #{} pending", trade.getId());
            return toResponse(trade);

        } catch (Exception ex) {
            log.error("SELL initiation failed for userId:{} symbol:{} — {}", req.getUserId(), symbol, ex.getMessage());
            throw new TradeExecutionException("Sell trade initiation failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<TradeResponse> getTradesByUser(Long userId, String authHeader) {
        validateUserAccess(userId);
        return tradeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Override
    public TradeResponse getTrade(Long id) {
        return toResponse(tradeRepository.findById(id).orElseThrow(() -> new TradeNotFoundException(id)));
    }

    private TradeResponse toResponse(Trade t) {
        return TradeResponse.builder().id(t.getId()).userId(t.getUserId()).symbol(t.getSymbol())
                .type(t.getType()).quantity(t.getQuantity()).price(t.getPrice())
                .totalAmount(t.getTotalAmount()).status(t.getStatus()).orderId(t.getOrderId())
                .createdAt(t.getCreatedAt()).build();
    }

    private JwtUser validateUserAccess(Long userId) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUser jwtUser)) {
            throw new org.springframework.security.access.AccessDeniedException("Access Denied");
        }
        if (!userId.equals(jwtUser.id())) {
            throw new org.springframework.security.access.AccessDeniedException("Access Denied");
        }
        return jwtUser;
    }

    private void publishTradeInitiatedAfterCommit(Long tradeId, io.stonk.trading.event.TradeInitiatedEvent event) {
        runAfterCommit(() ->
                kafkaTemplate.send(TradingDomainTopics.TRADE_INITIATED, String.valueOf(tradeId), event));
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
