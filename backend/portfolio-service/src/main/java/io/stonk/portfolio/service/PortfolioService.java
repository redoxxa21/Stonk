package io.stonk.portfolio.service;

import io.stonk.portfolio.dto.HoldingResponse;
import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {
    List<HoldingResponse> getPortfolio(Long userId, String bearerToken);
    HoldingResponse getHolding(Long userId, String symbol, String bearerToken);
    HoldingResponse addHolding(Long userId, String symbol, int quantity, BigDecimal price, String bearerToken);
    HoldingResponse reduceHolding(Long userId, String symbol, int quantity, BigDecimal price, String bearerToken);
}
