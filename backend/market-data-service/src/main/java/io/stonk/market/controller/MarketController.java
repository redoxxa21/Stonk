package io.stonk.market.controller;

import io.stonk.market.dto.StockResponse;
import io.stonk.market.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing market data endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/market/stocks")
public class MarketController {

    private final StockService stockService;

    public MarketController(StockService stockService) {
        this.stockService = stockService;
    }

    /** GET /market/stocks — list all available stocks */
    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    /** GET /market/stocks/{symbol} — get current price for a single stock */
    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }
}
