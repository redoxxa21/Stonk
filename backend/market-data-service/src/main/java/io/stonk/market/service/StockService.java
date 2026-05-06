package io.stonk.market.service;

import io.stonk.market.dto.StockResponse;

import java.util.List;

/**
 * Contract for market data operations.
 */
public interface StockService {

    /** Returns all available stocks with current prices. */
    List<StockResponse> getAllStocks();

    /** Returns a single stock by symbol. */
    StockResponse getStockBySymbol(String symbol);

    /**
     * Updates all stock prices with a random fluctuation.
     * Called by the price-update scheduler.
     */
    void updateAllPrices();
}
