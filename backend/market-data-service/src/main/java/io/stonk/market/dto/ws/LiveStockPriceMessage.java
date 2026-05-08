package io.stonk.market.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Live stock price update pushed to {@code /topic/stocks/{symbol}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveStockPriceMessage {

    private String symbol;
    private BigDecimal price;
    private BigDecimal changePercent;
    private long volume;
    private BigDecimal realizedVolatility;
    private BigDecimal liquidityScore;
    private long timestamp;
}
