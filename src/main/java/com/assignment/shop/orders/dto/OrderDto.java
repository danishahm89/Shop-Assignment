package com.assignment.shop.orders.dto;

import com.assignment.shop.orders.enums.OrderStatus;
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
    private List<OrderItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal orderTotal;
    private OrderStatus status;
    private LocalDateTime createdAt;

}
