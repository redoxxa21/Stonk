package io.stonk.order.dto;

import io.stonk.order.entity.OrderType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter @NoArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Type is required")
    private OrderType type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private BigDecimal price;
}
