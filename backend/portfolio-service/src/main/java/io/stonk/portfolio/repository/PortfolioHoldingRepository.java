package io.stonk.portfolio.repository;

import io.stonk.portfolio.entity.PortfolioHolding;
import io.stonk.portfolio.entity.PortfolioHoldingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, PortfolioHoldingId> {
    List<PortfolioHolding> findByUserId(Long userId);
    Optional<PortfolioHolding> findByUserIdAndSymbol(Long userId, String symbol);
}
