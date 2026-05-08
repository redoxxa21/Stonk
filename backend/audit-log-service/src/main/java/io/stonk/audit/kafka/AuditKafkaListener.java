package io.stonk.audit.kafka;

import io.stonk.audit.service.AuditIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditKafkaListener {

    private final AuditIngestService auditIngestService;

    /** Topics produced across the platform (auth → {@link AuditSubscribedTopics#USER_REGISTRATION}, trading → saga). */
    @KafkaListener(
            topics = {
                    AuditSubscribedTopics.USER_REGISTRATION,
                    AuditSubscribedTopics.TRADE_INITIATED,
                    AuditSubscribedTopics.TRADE_COMPLETED,
                    "wallet-debited",
                    "wallet-failed",
                    "wallet-credited",
                    "wallet-refund-requested",
                    "wallet-credit-requested",
                    "portfolio-add-requested",
                    "portfolio-added",
                    "portfolio-failed",
                    "portfolio-deducted",
                    "order-request",
                    "trade-executed",
                    "market-price-updated"
            },
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDomainEvent(ConsumerRecord<String, String> record) {
        try {
            auditIngestService.record(
                    record.topic(),
                    record.key(),
                    record.partition(),
                    record.offset(),
                    record.value());
        } catch (Exception ex) {
            log.error("Failed to persist audit for topic={}: {}", record.topic(), ex.getMessage());
        }
    }
}
