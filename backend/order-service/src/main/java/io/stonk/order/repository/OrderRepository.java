package io.stonk.order.repository;

import io.stonk.order.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<TradeOrder, Long> {
    List<TradeOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}
