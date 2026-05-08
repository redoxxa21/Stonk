package io.stonk.market.service;

import io.stonk.market.dto.StockResponse;

import java.util.List;

/** Contract for read-only market data (authoritative prices from executions). */
public interface StockService {

    List<StockResponse> getAllStocks();

    StockResponse getStockBySymbol(String symbol);
}
