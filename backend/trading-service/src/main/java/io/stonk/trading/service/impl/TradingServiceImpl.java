package io.stonk.trading.service.impl;

import io.stonk.trading.client.MarketDataClient;
import io.stonk.trading.client.OrderClient;
import io.stonk.trading.client.PortfolioClient;
import io.stonk.trading.client.UserDirectoryClient;
import io.stonk.trading.client.WalletClient;
import io.stonk.trading.dto.TradeRequest;
import io.stonk.trading.dto.TradeResponse;
import io.stonk.trading.dto.UserLookupResponse;
import io.stonk.trading.entity.Trade;
import io.stonk.trading.entity.TradeStatus;
import io.stonk.trading.entity.TradeType;
import io.stonk.trading.exception.TradeExecutionException;
import io.stonk.trading.exception.TradeNotFoundException;
import io.stonk.trading.repository.TradeRepository;
import io.stonk.trading.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TradingServiceImpl implements TradingService {

    private final TradeRepository tradeRepository;
    private final MarketDataClient marketDataClient;
    private final PortfolioClient portfolioClient;
    private final OrderClient orderClient;
    private final UserDirectoryClient userDirectoryClient;
    private final WalletClient walletClient;

    public TradingServiceImpl(TradeRepository tradeRepository,
                              MarketDataClient marketDataClient,
                              PortfolioClient portfolioClient,
                              OrderClient orderClient,
                              UserDirectoryClient userDirectoryClient,
                              WalletClient walletClient) {
        this.tradeRepository = tradeRepository;
        this.marketDataClient = marketDataClient;
        this.portfolioClient = portfolioClient;
        this.orderClient = orderClient;
        this.userDirectoryClient = userDirectoryClient;
        this.walletClient = walletClient;
    }

    /**
     * BUY flow:
     * 1. Get current price from Market Data Service
     * 2. Debit wallet (total cost)
     * 3. Add holding to Portfolio
     * 4. Create order → mark completed
     * 5. Save trade record
     */
    @Override
    @Transactional
    public TradeResponse executeBuy(TradeRequest req, String authHeader) {
        String symbol = req.getSymbol().toUpperCase();
        UserLookupResponse user = userDirectoryClient.getUserById(req.getUserId(), authHeader);
        log.info("Executing BUY: userId={}, symbol={}, qty={}", req.getUserId(), symbol, req.getQuantity());

        try {
            // 1. Get price
            BigDecimal price = marketDataClient.getCurrentPrice(symbol, authHeader);
            BigDecimal totalCost = price.multiply(BigDecimal.valueOf(req.getQuantity()));

            // 2. Debit wallet
            walletClient.debit(user.getId(), totalCost, authHeader);

            // 3. Add to portfolio
            portfolioClient.addHolding(user.getId(), symbol, req.getQuantity(), price, authHeader);

            // 3. Create and complete order
            Long orderId = orderClient.createOrder(user.getId(), symbol, "BUY", req.getQuantity(), price, authHeader);
            if (orderId != null) {
                orderClient.completeOrder(orderId, authHeader);
            }

            // 4. Save trade
            Trade trade = Trade.builder()
                    .userId(user.getId()).symbol(symbol).type(TradeType.BUY)
                    .quantity(req.getQuantity()).price(price).totalAmount(totalCost)
                    .status(TradeStatus.COMPLETED).orderId(orderId).build();
            tradeRepository.save(trade);

            log.info("BUY trade #{} completed: {} x {} @ {} = {}", trade.getId(), symbol, req.getQuantity(), price, totalCost);
            return toResponse(trade);

        } catch (TradeExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("BUY failed for userId:{} symbol:{} — {}", req.getUserId(), symbol, ex.getMessage());
            throw new TradeExecutionException("Buy trade failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * SELL flow:
     * 1. Get current price from Market Data Service
     * 2. Reduce holding in Portfolio (validates sufficient shares)
     * 3. Credit wallet (total proceeds)
     * 4. Create order → mark completed
     * 5. Save trade record
     */
    @Override
    @Transactional
    public TradeResponse executeSell(TradeRequest req, String authHeader) {
        String symbol = req.getSymbol().toUpperCase();
        UserLookupResponse user = userDirectoryClient.getUserById(req.getUserId(), authHeader);
        log.info("Executing SELL: userId={}, symbol={}, qty={}", req.getUserId(), symbol, req.getQuantity());

        try {
            // 1. Get price
            BigDecimal price = marketDataClient.getCurrentPrice(symbol, authHeader);
            BigDecimal totalProceeds = price.multiply(BigDecimal.valueOf(req.getQuantity()));

            // 2. Reduce portfolio (validates sufficient shares)
            portfolioClient.reduceHolding(user.getId(), symbol, req.getQuantity(), price, authHeader);

            // 3. Credit wallet
            walletClient.credit(user.getId(), totalProceeds, authHeader);

            // 3. Create and complete order
            Long orderId = orderClient.createOrder(user.getId(), symbol, "SELL", req.getQuantity(), price, authHeader);
            if (orderId != null) {
                orderClient.completeOrder(orderId, authHeader);
            }

            // 4. Save trade
            Trade trade = Trade.builder()
                    .userId(user.getId()).symbol(symbol).type(TradeType.SELL)
                    .quantity(req.getQuantity()).price(price).totalAmount(totalProceeds)
                    .status(TradeStatus.COMPLETED).orderId(orderId).build();
            tradeRepository.save(trade);

            log.info("SELL trade #{} completed: {} x {} @ {} = {}", trade.getId(), symbol, req.getQuantity(), price, totalProceeds);
            return toResponse(trade);

        } catch (TradeExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("SELL failed for userId:{} symbol:{} — {}", req.getUserId(), symbol, ex.getMessage());
            throw new TradeExecutionException("Sell trade failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<TradeResponse> getTradesByUser(Long userId, String authHeader) {
        userDirectoryClient.getUserById(userId, authHeader);
        return tradeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Override
    public TradeResponse getTrade(Long id) {
        return toResponse(tradeRepository.findById(id).orElseThrow(() -> new TradeNotFoundException(id)));
    }

    private TradeResponse toResponse(Trade t) {
        return TradeResponse.builder().id(t.getId()).userId(t.getUserId()).symbol(t.getSymbol())
                .type(t.getType()).quantity(t.getQuantity()).price(t.getPrice())
                .totalAmount(t.getTotalAmount()).status(t.getStatus()).orderId(t.getOrderId())
                .createdAt(t.getCreatedAt()).build();
    }
}
