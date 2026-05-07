package io.stonk.order.service.impl;

import io.stonk.order.security.JwtUser;
import io.stonk.order.dto.CreateOrderRequest;
import io.stonk.order.dto.OrderResponse;
import io.stonk.order.entity.OrderStatus;
import io.stonk.order.entity.TradeOrder;
import io.stonk.order.exception.OrderNotFoundException;
import io.stonk.order.repository.OrderRepository;
import io.stonk.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        JwtUser user = validateUserAccess(req.getUserId());
        BigDecimal total = req.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        TradeOrder order = TradeOrder.builder()
                .userId(user.id()).symbol(req.getSymbol().toUpperCase())
                .type(req.getType()).quantity(req.getQuantity()).price(req.getPrice())
                .totalAmount(total).status(OrderStatus.PENDING).build();
        orderRepository.save(order);
        log.info("Created {} order #{} for userId:{} — {} x {} @ {}",
                order.getType(), order.getId(), order.getUserId(), order.getSymbol(), order.getQuantity(), order.getPrice());
        return toResponse(order);
    }

    @Override
    public OrderResponse getOrder(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public List<OrderResponse> getOrdersByUser(Long userId) {
        validateUserAccess(userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(Long id) {
        TradeOrder order = findById(id);
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        log.info("Completed order #{}", id);
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        TradeOrder order = findById(id);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Cancelled order #{}", id);
        return toResponse(order);
    }

    private TradeOrder findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private OrderResponse toResponse(TradeOrder o) {
        return OrderResponse.builder().id(o.getId()).userId(o.getUserId()).symbol(o.getSymbol())
                .type(o.getType()).quantity(o.getQuantity()).price(o.getPrice())
                .totalAmount(o.getTotalAmount()).status(o.getStatus())
                .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt()).build();
    }

    private JwtUser validateUserAccess(Long userId) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUser jwtUser)) {
            throw new org.springframework.security.access.AccessDeniedException("Access Denied");
        }
        if (!userId.equals(jwtUser.id())) {
            throw new org.springframework.security.access.AccessDeniedException("Access Denied");
        }
        return jwtUser;
    }
}
