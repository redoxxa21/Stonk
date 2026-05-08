package io.stonk.sim.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.sim.dto.MarketPriceUpdatedEvent;
import io.stonk.sim.state.MarketTick;
import io.stonk.sim.state.MarketViewRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketPriceUpdatedListener {

    private final ObjectMapper objectMapper;
    private final MarketViewRegistry marketViewRegistry;

    @KafkaListener(
            topics = SimulationKafkaTopics.MARKET_PRICE_UPDATED,
            groupId = "${spring.application.name}-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPriceUpdate(String json) {
        try {
            MarketPriceUpdatedEvent e = objectMapper.readValue(json, MarketPriceUpdatedEvent.class);
            marketViewRegistry.updateFromEvent(e.getSymbol(), new MarketTick(
                    e.getLastPrice(),
                    e.getChangePercent(),
                    e.getRealizedVolatility(),
                    e.getLiquidityScore()));
        } catch (Exception ex) {
            log.debug("market-price-updated parse skipped: {}", ex.getMessage());
        }
    }
}
