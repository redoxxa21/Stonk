package io.stonk.wallet.service;

import io.stonk.wallet.entity.TradeType;
import io.stonk.wallet.event.*;
import io.stonk.wallet.exception.InsufficientFundsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletSagaListener {

    private final WalletService walletService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "trade-initiated", groupId = "${spring.application.name}-group")
    public void onTradeInitiated(TradeInitiatedEvent event) {
        log.info("Received TradeInitiatedEvent for tradeId: {}", event.getTradeId());
        
        if (event.getType() == TradeType.BUY) {
            try {
                walletService.debit(event.getUserId(), event.getTotalAmount(), "Trade BUY #" + event.getTradeId());
                log.info("Successfully debited wallet for tradeId: {}", event.getTradeId());
                
                WalletDebitedEvent successEvent = WalletDebitedEvent.builder()
                        .tradeId(event.getTradeId())
                        .userId(event.getUserId())
                        .amount(event.getTotalAmount())
                        .build();
                kafkaTemplate.send("wallet-debited", successEvent);
                
            } catch (InsufficientFundsException ex) {
                log.error("Insufficient funds for tradeId: {}", event.getTradeId());
                WalletFailedEvent failedEvent = WalletFailedEvent.builder()
                        .tradeId(event.getTradeId())
                        .userId(event.getUserId())
                        .amount(event.getTotalAmount())
                        .reason("Insufficient funds")
                        .build();
                kafkaTemplate.send("wallet-failed", failedEvent);
            } catch (Exception ex) {
                log.error("Wallet debit failed for tradeId: {}: {}", event.getTradeId(), ex.getMessage());
                WalletFailedEvent failedEvent = WalletFailedEvent.builder()
                        .tradeId(event.getTradeId())
                        .userId(event.getUserId())
                        .amount(event.getTotalAmount())
                        .reason("Unexpected error: " + ex.getMessage())
                        .build();
                kafkaTemplate.send("wallet-failed", failedEvent);
            }
        }
    }

    @KafkaListener(topics = "wallet-refund-requested", groupId = "${spring.application.name}-group")
    public void onWalletRefundRequested(WalletRefundRequestedEvent event) {
        log.info("Received WalletRefundRequestedEvent for tradeId: {}", event.getTradeId());
        walletService.credit(event.getUserId(), event.getAmount(), "Refund Trade #" + event.getTradeId() + " (" + event.getReason() + ")");
        log.info("Successfully refunded wallet for tradeId: {}", event.getTradeId());
    }

    @KafkaListener(topics = "wallet-credit-requested", groupId = "${spring.application.name}-group")
    public void onWalletCreditRequested(WalletCreditRequestedEvent event) {
        log.info("Received WalletCreditRequestedEvent for tradeId: {}", event.getTradeId());
        walletService.credit(event.getUserId(), event.getAmount(), "Trade SELL #" + event.getTradeId());
        log.info("Successfully credited wallet for tradeId: {}", event.getTradeId());
        
        WalletCreditedEvent creditedEvent = WalletCreditedEvent.builder()
                .tradeId(event.getTradeId())
                .userId(event.getUserId())
                .amount(event.getAmount())
                .build();
        kafkaTemplate.send("wallet-credited", creditedEvent);
    }
}
