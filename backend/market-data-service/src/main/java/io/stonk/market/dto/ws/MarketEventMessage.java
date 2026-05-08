package io.stonk.market.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Market event pushed to {@code /topic/market/events}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketEventMessage {

    private String eventType;
    private String symbol;
    private double severity;
    private String message;
    private long timestamp;
}
