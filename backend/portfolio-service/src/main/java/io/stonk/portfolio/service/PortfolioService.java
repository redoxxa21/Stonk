package io.stonk.portfolio.service;

import io.stonk.portfolio.dto.HoldingResponse;
import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {
    List<HoldingResponse> getPortfolio(Long userId);
    HoldingResponse getHolding(Long userId, String symbol);
    HoldingResponse addHolding(Long userId, String symbol, int quantity, BigDecimal price);
    HoldingResponse reduceHolding(Long userId, String symbol, int quantity, BigDecimal price);
    void addHoldingSaga(Long userId, String symbol, int quantity, BigDecimal price);
    void reduceHoldingSaga(Long userId, String symbol, int quantity, BigDecimal price);
}
