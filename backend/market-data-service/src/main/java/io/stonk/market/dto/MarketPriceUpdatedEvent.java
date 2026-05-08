package io.stonk.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** Published after each applied execution for bot feedback and dashboards. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPriceUpdatedEvent {

    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal changePercent;
    private long cumulativeVolume;
    private BigDecimal realizedVolatility;
    private BigDecimal liquidityScore;
    private BigDecimal candleOpen;
    private BigDecimal candleHigh;
    private BigDecimal candleLow;
    private BigDecimal candleClose;
    private long candleVolume;
    private long candleMinuteEpoch;
    private Instant publishedAt;
}
