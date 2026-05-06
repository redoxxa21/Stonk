package io.stonk.wallet.controller;

import io.stonk.wallet.dto.AmountRequest;
import io.stonk.wallet.dto.TransactionResponse;
import io.stonk.wallet.dto.WalletResponse;
import io.stonk.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{userId}/create")
    public ResponseEntity<WalletResponse> create(@PathVariable Long userId,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(userId, authorization));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> get(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWallet(userId));
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<WalletResponse> deposit(@PathVariable Long userId,
                                                   @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(walletService.deposit(userId, req.getAmount()));
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<WalletResponse> withdraw(@PathVariable Long userId,
                                                    @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(walletService.withdraw(userId, req.getAmount()));
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<WalletResponse> debit(@PathVariable Long userId,
                                                 @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(walletService.debit(userId, req.getAmount(), "Trade debit"));
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<WalletResponse> credit(@PathVariable Long userId,
                                                  @Valid @RequestBody AmountRequest req) {
        return ResponseEntity.ok(walletService.credit(userId, req.getAmount(), "Trade credit"));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<TransactionResponse>> transactions(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getTransactions(userId));
    }
}
