package io.stonk.market.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Compact stock snapshot used inside {@link MarketOverviewMessage}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSnapshot {

    private String symbol;
    private String name;
    private BigDecimal price;
    private BigDecimal changePercent;
}
