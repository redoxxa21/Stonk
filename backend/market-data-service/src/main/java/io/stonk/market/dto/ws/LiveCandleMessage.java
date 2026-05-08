package io.stonk.market.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Live candle update pushed to {@code /topic/candles/{symbol}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveCandleMessage {

    private String symbol;
    private String timeframe;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private long volume;
    private long minuteEpoch;
}
