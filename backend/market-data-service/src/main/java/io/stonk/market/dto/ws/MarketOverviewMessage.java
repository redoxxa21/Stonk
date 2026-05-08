package io.stonk.market.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Market overview pushed to {@code /topic/market/overview}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketOverviewMessage {

    private List<StockSnapshot> topGainers;
    private List<StockSnapshot> topLosers;
    private String marketStatus;
    private long timestamp;
}
