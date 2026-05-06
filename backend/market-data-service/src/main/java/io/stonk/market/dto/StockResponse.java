package io.stonk.market.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outbound DTO representing a stock's current market data.
 */
@Getter
@Builder
public class StockResponse {

    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal changePercent;
    private LocalDateTime lastUpdated;
}
