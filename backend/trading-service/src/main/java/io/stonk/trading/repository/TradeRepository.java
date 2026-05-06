package io.stonk.trading.repository;

import io.stonk.trading.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserIdOrderByCreatedAtDesc(Long userId);
}
