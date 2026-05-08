package io.stonk.market.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a stock listed in the simulated market.
 *
 * <p>Prices move only when {@link io.stonk.market.kafka.TradeExecutedConsumer}
 * applies exchange executions (no synthetic random walk).
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

    /** Running share volume in the simulation (sum of executed sizes). */
    @Column(nullable = false)
    private long cumulativeVolume;

    /** Simple annualized volatility proxy from recent returns (simulation metric). */
    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal realizedVolatility;

    /** Higher is calmer (more volume per unit vol); derived for bots. */
    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal liquidityScore;
}
