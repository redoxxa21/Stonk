package io.stonk.market.service.impl;

import io.stonk.market.dto.StockResponse;
import io.stonk.market.entity.Stock;
import io.stonk.market.exception.StockNotFoundException;
import io.stonk.market.repository.StockRepository;
import io.stonk.market.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findById(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException(symbol));
        return toResponse(stock);
    }

    private StockResponse toResponse(Stock stock) {
        return StockResponse.builder()
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .currentPrice(stock.getCurrentPrice())
                .previousClose(stock.getPreviousClose())
                .changePercent(stock.getChangePercent())
                .lastUpdated(stock.getLastUpdated())
                .cumulativeVolume(stock.getCumulativeVolume())
                .realizedVolatility(stock.getRealizedVolatility())
                .liquidityScore(stock.getLiquidityScore())
                .build();
    }
}
