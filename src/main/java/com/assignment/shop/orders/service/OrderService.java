package com.assignment.shop.orders.service;

import com.assignment.shop.discounts.DiscountStrategy;
import com.assignment.shop.discounts.strategy.DiscountReportDto;
import com.assignment.shop.exceptions.ResourceNotFoundException;
import com.assignment.shop.orders.dto.OrderDto;
import com.assignment.shop.orders.dto.OrderItemDto;
import com.assignment.shop.orders.entity.Order;
import com.assignment.shop.orders.entity.OrderItem;
import com.assignment.shop.orders.enums.OrderStatus;
import com.assignment.shop.orders.repo.OrderRepository;
import com.assignment.shop.products.entity.Product;
import com.assignment.shop.products.enums.ProductStatus;
import com.assignment.shop.products.repo.ProductRepository;
import com.assignment.shop.users.entity.User;
import com.assignment.shop.users.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final DiscountStrategy discountEngine;

    @Transactional
    public OrderDto placeOrder(Long userId, List<OrderItemDto> itemsReq) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto itemRequest : itemsReq) {
            Product product = productRepo.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (!product.getStatus().equalsIgnoreCase(ProductStatus.ACTIVE.name())) {
                throw new ResourceNotFoundException("Product not available");
            }

            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new ResourceNotFoundException("Insufficient stock for " + product.getName());
            }

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemTotal);

            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepo.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice().setScale(2, RoundingMode.HALF_UP));
            orderItem.setDiscountApplied(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);
        }

        DiscountReportDto discountReport = discountEngine.evaluate(user, subtotal);
        BigDecimal totalDiscount = discountReport.getTotalDiscount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = discountReport.getFinalAmount().setScale(2, RoundingMode.HALF_UP);

        Order order = new Order();
        order.setUser(user);
        order.setOrderTotal(finalTotal);
        order.setStatus(OrderStatus.PLACED);
        orderItems.forEach(order::addItem);
        Order savedOrder = orderRepo.save(order);

        return OrderDto.builder()
                .id(savedOrder.getId())
                .userId(userId)
                .items(mapItemsToDto(savedOrder.getItems()))
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .discountAmount(totalDiscount)
                .orderTotal(finalTotal)
                .status(savedOrder.getStatus())
                .createdAt(savedOrder.getCreatedAt())
                .build();
    }

    private List<OrderItemDto> mapItemsToDto(List<OrderItem> items) {
        return items.stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .discountApplied(item.getDiscountApplied())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .toList();
    }
}
