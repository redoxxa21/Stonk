package io.stonk.trading.service;

import io.stonk.trading.entity.Trade;
import io.stonk.trading.entity.TradeStatus;
import io.stonk.trading.entity.TradeType;
import io.stonk.trading.event.*;
import io.stonk.trading.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSagaListener {

    private final TradeRepository tradeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @KafkaListener(topics = "wallet-debited", groupId = "${spring.application.name}-group")
    @Transactional
    public void onWalletDebited(WalletDebitedEvent event) {
        log.info("[Saga] Wallet debited for tradeId: {}. Requesting portfolio add...", event.getTradeId());
        Trade trade = tradeRepository.findById(event.getTradeId()).orElseThrow();
        
        PortfolioAddRequestedEvent addReq = PortfolioAddRequestedEvent.builder()
                .tradeId(trade.getId())
                .userId(trade.getUserId())
                .symbol(trade.getSymbol())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .build();
        kafkaTemplate.send("portfolio-add-requested", addReq);
    }

    @KafkaListener(topics = "wallet-failed", groupId = "${spring.application.name}-group")
    @Transactional
    public void onWalletFailed(WalletFailedEvent event) {
        log.error("[Saga] Wallet failed for tradeId: {}. Reason: {}. Marking Trade as FAILED.", event.getTradeId(), event.getReason());
        Trade trade = tradeRepository.findById(event.getTradeId()).orElseThrow();
        trade.setStatus(TradeStatus.FAILED);
        tradeRepository.save(trade);
    }

    @KafkaListener(topics = "portfolio-added", groupId = "${spring.application.name}-group")
    @Transactional
    public void onPortfolioAdded(PortfolioAddedEvent event) {
        log.info("[Saga] Portfolio added for tradeId: {}. Trade is COMPLETED.", event.getTradeId());
        Trade trade = tradeRepository.findById(event.getTradeId()).orElseThrow();
        trade.setStatus(TradeStatus.COMPLETED);
        tradeRepository.save(trade);

        TradeCompletedEvent completedEvent = TradeCompletedEvent.builder()
                .tradeId(trade.getId())
                .userId(trade.getUserId())
                .symbol(trade.getSymbol())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .type(trade.getType())
                .build();
        kafkaTemplate.send("trade-completed", completedEvent);
    }

    @KafkaListener(topics = "portfolio-failed", groupId = "${spring.application.name}-group")
    @Transactional
    public void onPortfolioFailed(PortfolioFailedEvent event) {
        log.error("[Saga] Portfolio failed for tradeId: {}. Reason: {}", event.getTradeId(), event.getReason());
        Trade trade = tradeRepository.findById(event.getTradeId()).orElseThrow();
        
        if (trade.getType() == TradeType.BUY) {
            log.info("[Saga] Requesting wallet refund for failed BUY tradeId: {}", trade.getId());
            WalletRefundRequestedEvent refundEvent = WalletRefundRequestedEvent.builder()
                    .tradeId(trade.getId())
                    .userId(trade.getUserId())
                    .amount(trade.getTotalAmount())
                    .reason("Portfolio add failed: " + event.getReason())
                    .build();
            kafkaTemplate.send("wallet-refund-requested", refundEvent);
        }
        
        trade.setStatus(TradeStatus.FAILED);
        tradeRepository.save(trade);
    }

    @KafkaListener(topics = "portfolio-deducted", groupId = "${spring.application.name}-group")
    @Transactional
    public void onPortfolioDeducted(PortfolioDeductedEvent event) {
        log.info("[Saga] Portfolio deducted for tradeId: {}. Requesting wallet credit...", event.getTradeId());
        Trade trade = tradeRepository.findById(event.getTradeId()).orElseThrow();
        
        WalletCreditRequestedEvent creditReq = WalletCreditRequestedEvent.builder()
                .tradeId(trade.getId())
                .userId(trade.getUserId())
                .amount(trade.getTotalAmount())
                .reason("Trade SELL #" + trade.getId())
                .build();
        kafkaTemplate.send("wallet-credit-requested", creditReq);
    }

    @KafkaListener(topics = "wallet-credited", groupId = "${spring.application.name}-group")
    @Transactional
    public void onWalletCredited(WalletCreditedEvent event) {
        log.info("[Saga] Wallet credited for tradeId: {}. Trade is COMPLETED.", event.getTradeId());
        Trade trade = tradeRepository.findById(event.getTradeId()).orElseThrow();
        trade.setStatus(TradeStatus.COMPLETED);
        tradeRepository.save(trade);

        TradeCompletedEvent completedEvent = TradeCompletedEvent.builder()
                .tradeId(trade.getId())
                .userId(trade.getUserId())
                .symbol(trade.getSymbol())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .type(trade.getType())
                .build();
        kafkaTemplate.send("trade-completed", completedEvent);
    }
}
