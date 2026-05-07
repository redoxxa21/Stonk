package io.stonk.order.service;

import io.stonk.order.dto.CreateOrderRequest;
import io.stonk.order.dto.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrder(Long id);
    List<OrderResponse> getOrdersByUser(Long userId);
    OrderResponse completeOrder(Long id);
    OrderResponse cancelOrder(Long id);
}
