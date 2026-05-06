package io.stonk.wallet.dto;

import io.stonk.wallet.entity.TransactionType;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime createdAt;
}
