package io.stonk.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_holdings")
@IdClass(PortfolioHoldingId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioHolding {

    @Id
    @Column(nullable = false)
    private Long userId;

    @Id
    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal averagePrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInvested;
}
