package io.stonk.portfolio.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class WalletLookupResponse {
    private Long id;
    private String username;
    private BigDecimal balance;
    private String currency;
}
