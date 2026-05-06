package io.stonk.trading.service;

import io.stonk.trading.dto.TradeRequest;
import io.stonk.trading.dto.TradeResponse;
import java.util.List;

public interface TradingService {
    TradeResponse executeBuy(TradeRequest request, String authHeader);
    TradeResponse executeSell(TradeRequest request, String authHeader);
    List<TradeResponse> getTradesByUser(Long userId, String authHeader);
    TradeResponse getTrade(Long id);
}
