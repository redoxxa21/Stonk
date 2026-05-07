package io.stonk.portfolio.service.impl;

import io.stonk.portfolio.security.JwtUser;
import io.stonk.portfolio.dto.HoldingResponse;
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
    public PortfolioServiceImpl(PortfolioHoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    @Override
    public List<HoldingResponse> getPortfolio(Long userId) {
        JwtUser user = validateUserAccess(userId);
        return holdingRepository.findByUserId(user.id()).stream()
                .map(holding -> toResponse(holding, user.username()))
                .toList();
    }

    @Override
    public HoldingResponse getHolding(Long userId, String symbol) {
        JwtUser user = validateUserAccess(userId);
        PortfolioHolding h = holdingRepository.findByUserIdAndSymbol(user.id(), symbol.toUpperCase())
                .orElseThrow(() -> new HoldingNotFoundException(userId, symbol));
        return toResponse(h, user.username());
    }

    @Override
    @Transactional
    public HoldingResponse addHolding(Long userId,
                                      String symbol,
                                      int quantity,
                                      BigDecimal price) {
        JwtUser user = validateUserAccess(userId);
        String sym = symbol.toUpperCase();
        BigDecimal cost = price.multiply(BigDecimal.valueOf(quantity));

        Optional<PortfolioHolding> existing = holdingRepository.findByUserIdAndSymbol(user.id(), sym);

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
                    .userId(user.id()).symbol(sym).quantity(quantity)
                    .averagePrice(price).totalInvested(cost).build();
        }

        holdingRepository.save(holding);

        log.info("Added {} shares of {} for userId:{} @ {}", quantity, sym, user.id(), price);
        return toResponse(holding, user.username());
    }

    @Override
    @Transactional
    public HoldingResponse reduceHolding(Long userId, String symbol, int quantity, BigDecimal price) {
        JwtUser user = validateUserAccess(userId);
        String sym = symbol.toUpperCase();
        BigDecimal proceeds = price.multiply(BigDecimal.valueOf(quantity));
        PortfolioHolding holding = holdingRepository.findByUserIdAndSymbol(user.id(), sym)
                .orElseThrow(() -> new HoldingNotFoundException(userId, sym));

        if (holding.getQuantity() < quantity) {
            throw new InsufficientHoldingException(sym, quantity, holding.getQuantity());
        }

        int newQty = holding.getQuantity() - quantity;
        if (newQty == 0) {
            holdingRepository.delete(holding);

            log.info("Sold all {} shares of {} for userId:{}", quantity, sym, user.id());
            return HoldingResponse.builder().id(user.id()).username(user.username()).symbol(sym).quantity(0)
                    .averagePrice(BigDecimal.ZERO).totalInvested(BigDecimal.ZERO).build();
        }

        BigDecimal soldPortion = holding.getAveragePrice().multiply(BigDecimal.valueOf(quantity));
        holding.setQuantity(newQty);
        holding.setTotalInvested(holding.getTotalInvested().subtract(soldPortion));
        holdingRepository.save(holding);

        log.info("Reduced {} shares of {} for userId:{}, remaining: {}", quantity, sym, user.id(), newQty);
        return toResponse(holding, user.username());
    }

    @Transactional
    public void addHoldingSaga(Long userId, String symbol, int quantity, BigDecimal price) {
        String sym = symbol.toUpperCase();
        BigDecimal cost = price.multiply(BigDecimal.valueOf(quantity));
        Optional<PortfolioHolding> existing = holdingRepository.findByUserIdAndSymbol(userId, sym);

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
                    .userId(userId).symbol(sym).quantity(quantity)
                    .averagePrice(price).totalInvested(cost).build();
        }
        holdingRepository.save(holding);
        log.info("[SAGA] Added {} shares of {} for userId:{} @ {}", quantity, sym, userId, price);
    }

    @Transactional
    public void reduceHoldingSaga(Long userId, String symbol, int quantity, BigDecimal price) {
        String sym = symbol.toUpperCase();
        PortfolioHolding holding = holdingRepository.findByUserIdAndSymbol(userId, sym)
                .orElseThrow(() -> new HoldingNotFoundException(userId, sym));

        if (holding.getQuantity() < quantity) {
            throw new InsufficientHoldingException(sym, quantity, holding.getQuantity());
        }

        int newQty = holding.getQuantity() - quantity;
        if (newQty == 0) {
            holdingRepository.delete(holding);
            log.info("[SAGA] Sold all {} shares of {} for userId:{}", quantity, sym, userId);
            return;
        }

        BigDecimal soldPortion = holding.getAveragePrice().multiply(BigDecimal.valueOf(quantity));
        holding.setQuantity(newQty);
        holding.setTotalInvested(holding.getTotalInvested().subtract(soldPortion));
        holdingRepository.save(holding);
        log.info("[SAGA] Reduced {} shares of {} for userId:{}, remaining: {}", quantity, sym, userId, newQty);
    }

    private JwtUser validateUserAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUser jwtUser)) {
            throw new UserAccessDeniedException(userId);
        }
        if (!userId.equals(jwtUser.id())) {
            throw new UserAccessDeniedException(userId);
        }
        return jwtUser;
    }

    private HoldingResponse toResponse(PortfolioHolding h, String username) {
        return HoldingResponse.builder().id(h.getUserId()).username(username).symbol(h.getSymbol())
                .quantity(h.getQuantity()).averagePrice(h.getAveragePrice())
                .totalInvested(h.getTotalInvested()).build();
    }
}
