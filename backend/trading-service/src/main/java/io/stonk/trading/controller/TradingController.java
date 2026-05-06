package io.stonk.trading.controller;

import io.stonk.trading.dto.TradeRequest;
import io.stonk.trading.dto.TradeResponse;
import io.stonk.trading.service.TradingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trades")
public class TradingController {

    private final TradingService tradingService;
    public TradingController(TradingService tradingService) { this.tradingService = tradingService; }

    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buy(@Valid @RequestBody TradeRequest req, HttpServletRequest httpReq) {
        String auth = httpReq.getHeader("Authorization");
        return ResponseEntity.status(HttpStatus.CREATED).body(tradingService.executeBuy(req, auth));
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sell(@Valid @RequestBody TradeRequest req, HttpServletRequest httpReq) {
        String auth = httpReq.getHeader("Authorization");
        return ResponseEntity.status(HttpStatus.CREATED).body(tradingService.executeSell(req, auth));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<TradeResponse>> getByUser(@PathVariable Long userId, HttpServletRequest httpReq) {
        return ResponseEntity.ok(tradingService.getTradesByUser(userId, httpReq.getHeader("Authorization")));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<TradeResponse> getTrade(@PathVariable Long id) {
        return ResponseEntity.ok(tradingService.getTrade(id));
    }
}
