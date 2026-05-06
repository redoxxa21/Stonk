package io.stonk.portfolio.service.impl;

import io.stonk.portfolio.client.UserDirectoryClient;
import io.stonk.portfolio.client.WalletClient;
import io.stonk.portfolio.dto.HoldingResponse;
import io.stonk.portfolio.dto.UserLookupResponse;
import io.stonk.portfolio.entity.PortfolioHolding;
import io.stonk.portfolio.exception.HoldingNotFoundException;
import io.stonk.portfolio.exception.InsufficientHoldingException;
import io.stonk.portfolio.exception.UserAccessDeniedException;
import io.stonk.portfolio.repository.PortfolioHoldingRepository;
import io.stonk.portfolio.service.PortfolioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioHoldingRepository holdingRepository;
    private final UserDirectoryClient userDirectoryClient;
    private final WalletClient walletClient;

    public PortfolioServiceImpl(PortfolioHoldingRepository holdingRepository,
                                UserDirectoryClient userDirectoryClient,
                                WalletClient walletClient) {
        this.holdingRepository = holdingRepository;
        this.userDirectoryClient = userDirectoryClient;
        this.walletClient = walletClient;
    }

    @Override
    public List<HoldingResponse> getPortfolio(Long userId, String bearerToken) {
        UserLookupResponse user = validateUserAccess(userId, bearerToken);
        return holdingRepository.findByUserId(user.getId()).stream()
                .map(holding -> toResponse(holding, user.getUsername()))
                .toList();
    }

    @Override
    public HoldingResponse getHolding(Long userId, String symbol, String bearerToken) {
        UserLookupResponse user = validateUserAccess(userId, bearerToken);
        PortfolioHolding h = holdingRepository.findByUserIdAndSymbol(user.getId(), symbol.toUpperCase())
                .orElseThrow(() -> new HoldingNotFoundException(userId, symbol));
        return toResponse(h, user.getUsername());
    }

    @Override
    @Transactional
    public HoldingResponse addHolding(Long userId,
                                      String symbol,
                                      int quantity,
                                      BigDecimal price,
                                      String bearerToken) {
        UserLookupResponse user = validateUserAccess(userId, bearerToken);
        String sym = symbol.toUpperCase();
        BigDecimal cost = price.multiply(BigDecimal.valueOf(quantity));

        Optional<PortfolioHolding> existing = holdingRepository.findByUserIdAndSymbol(user.getId(), sym);

        PortfolioHolding holding;
        if (existing.isPresent()) {
            holding = existing.get();
            int newQty = holding.getQuantity() + quantity;
            BigDecimal newInvested = holding.getTotalInvested().add(cost);
            BigDecimal newAvg = newInvested.divide(BigDecimal.valueOf(newQty), 4, RoundingMode.HALF_UP);
            holding.setQuantity(newQty);
            holding.setAveragePrice(newAvg);
            holding.setTotalInvested(newInvested);
        } else {
            holding = PortfolioHolding.builder()
                    .userId(user.getId()).symbol(sym).quantity(quantity)
                    .averagePrice(price).totalInvested(cost).build();
        }

        holdingRepository.save(holding);
        walletClient.debit(user.getId(), cost, bearerToken);
        log.info("Added {} shares of {} for userId:{} @ {}", quantity, sym, user.getId(), price);
        return toResponse(holding, user.getUsername());
    }

    @Override
    @Transactional
    public HoldingResponse reduceHolding(Long userId, String symbol, int quantity, BigDecimal price, String bearerToken) {
        UserLookupResponse user = validateUserAccess(userId, bearerToken);
        String sym = symbol.toUpperCase();
        BigDecimal proceeds = price.multiply(BigDecimal.valueOf(quantity));
        PortfolioHolding holding = holdingRepository.findByUserIdAndSymbol(user.getId(), sym)
                .orElseThrow(() -> new HoldingNotFoundException(userId, sym));

        if (holding.getQuantity() < quantity) {
            throw new InsufficientHoldingException(sym, quantity, holding.getQuantity());
        }

        int newQty = holding.getQuantity() - quantity;
        if (newQty == 0) {
            holdingRepository.delete(holding);
            walletClient.credit(user.getId(), proceeds, bearerToken);
            log.info("Sold all {} shares of {} for userId:{}", quantity, sym, user.getId());
            return HoldingResponse.builder().id(user.getId()).username(user.getUsername()).symbol(sym).quantity(0)
                    .averagePrice(BigDecimal.ZERO).totalInvested(BigDecimal.ZERO).build();
        }

        BigDecimal soldPortion = holding.getAveragePrice().multiply(BigDecimal.valueOf(quantity));
        holding.setQuantity(newQty);
        holding.setTotalInvested(holding.getTotalInvested().subtract(soldPortion));
        holdingRepository.save(holding);
        walletClient.credit(user.getId(), proceeds, bearerToken);
        log.info("Reduced {} shares of {} for userId:{}, remaining: {}", quantity, sym, user.getId(), newQty);
        return toResponse(holding, user.getUsername());
    }

    private UserLookupResponse validateUserAccess(Long userId, String bearerToken) {
        UserLookupResponse user = userDirectoryClient.getUserById(userId, bearerToken);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUsername = authentication != null ? authentication.getName() : null;
        if (authenticatedUsername == null || !authenticatedUsername.equals(user.getUsername())) {
            throw new UserAccessDeniedException(userId);
        }
        return user;
    }

    private HoldingResponse toResponse(PortfolioHolding h, String username) {
        return HoldingResponse.builder().id(h.getUserId()).username(username).symbol(h.getSymbol())
                .quantity(h.getQuantity()).averagePrice(h.getAveragePrice())
                .totalInvested(h.getTotalInvested()).build();
    }
}
