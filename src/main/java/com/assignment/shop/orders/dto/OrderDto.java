package com.assignment.shop.orders.dto;

import com.assignment.shop.orders.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;

    @NotNull(message = "Order items are required")
    @NotEmpty(message = "Order must contain at least one item")
    @Size(max = 100, message = "Order cannot contain more than 100 items")
    @Valid  // Enable nested validation for OrderItemDto
    private List<OrderItemDto> items;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal orderTotal;
    private OrderStatus status;
    private LocalDateTime createdAt;

}
