package io.stonk.market.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a stock listed in the simulated market.
 *
 * <p>The {@code symbol} is the primary key (e.g. {@code "AAPL"}).
 * Prices are updated periodically by {@link io.stonk.market.scheduler.PriceUpdateScheduler}.
 */
@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @Column(length = 10)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal previousClose;

    /** Percentage change from previous close. Can be negative. */
    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal changePercent;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;
}
