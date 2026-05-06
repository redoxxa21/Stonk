package io.stonk.portfolio.controller;

import io.stonk.portfolio.dto.HoldingResponse;
import io.stonk.portfolio.dto.TradeHoldingRequest;
import io.stonk.portfolio.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<HoldingResponse>> getPortfolio(@PathVariable Long userId, HttpServletRequest httpReq) {
        return ResponseEntity.ok(portfolioService.getPortfolio(userId, httpReq.getHeader("Authorization")));
    }

    @GetMapping("/{userId}/holding/{symbol}")
    public ResponseEntity<HoldingResponse> getHolding(@PathVariable Long userId, @PathVariable String symbol, HttpServletRequest httpReq) {
        return ResponseEntity.ok(portfolioService.getHolding(userId, symbol, httpReq.getHeader("Authorization")));
    }

    @PostMapping("/{userId}/buy")
    public ResponseEntity<HoldingResponse> buy(@PathVariable Long userId,
                                                @Valid @RequestBody TradeHoldingRequest req,
                                                HttpServletRequest httpReq) {
        return ResponseEntity.ok(portfolioService.addHolding(
                userId,
                req.getSymbol(),
                req.getQuantity(),
                req.getPrice(),
                httpReq.getHeader("Authorization")
        ));
    }

    @PostMapping("/{userId}/sell")
    public ResponseEntity<HoldingResponse> sell(@PathVariable Long userId,
                                                 @Valid @RequestBody TradeHoldingRequest req,
                                                 HttpServletRequest httpReq) {
        return ResponseEntity.ok(portfolioService.reduceHolding(
                userId,
                req.getSymbol(),
                req.getQuantity(),
                req.getPrice(),
                httpReq.getHeader("Authorization")
        ));
    }
}
