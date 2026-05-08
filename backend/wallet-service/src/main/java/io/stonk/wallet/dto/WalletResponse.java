package io.stonk.wallet.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @Builder
public class WalletResponse {
    private Long id;
    private Long userId;
    private String username;
    private BigDecimal balance;
    private String currency;
}
