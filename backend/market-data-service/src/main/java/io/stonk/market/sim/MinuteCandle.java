package io.stonk.market.sim;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MinuteCandle {

    private final long minuteEpoch;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private long volume;

    public MinuteCandle(long minuteEpoch, BigDecimal firstPrice, int qty) {
        this.minuteEpoch = minuteEpoch;
        this.open = firstPrice;
        this.high = firstPrice;
        this.low = firstPrice;
        this.close = firstPrice;
        this.volume = qty;
    }

    public void add(BigDecimal price, int qty) {
        if (high.compareTo(price) < 0) {
            high = price;
        }
        if (low.compareTo(price) > 0) {
            low = price;
        }
        close = price;
        volume += qty;
    }
}
