package io.stonk.sim.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketPriceUpdatedEvent {
    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal changePercent;
    private BigDecimal realizedVolatility;
    private BigDecimal liquidityScore;
}
