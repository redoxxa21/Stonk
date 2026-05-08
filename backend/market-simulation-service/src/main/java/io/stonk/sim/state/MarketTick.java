package io.stonk.sim.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketTick {
    private BigDecimal lastPrice;
    private BigDecimal changePercent;
    private BigDecimal volatility;
    private BigDecimal liquidity;
}
