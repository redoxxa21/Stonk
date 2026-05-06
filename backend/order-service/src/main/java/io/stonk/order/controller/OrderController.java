package io.stonk.order.controller;

import io.stonk.order.dto.CreateOrderRequest;
import io.stonk.order.dto.OrderResponse;
import io.stonk.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req, HttpServletRequest httpReq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(req, httpReq.getHeader("Authorization")));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderResponse>> getByUser(@PathVariable Long userId, HttpServletRequest httpReq) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId, httpReq.getHeader("Authorization")));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<OrderResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.completeOrder(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
