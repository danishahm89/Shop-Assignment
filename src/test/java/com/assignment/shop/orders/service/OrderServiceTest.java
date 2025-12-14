package com.assignment.shop.orders.service;

import com.assignment.shop.discounts.DiscountStrategy;
import com.assignment.shop.discounts.strategy.DiscountReportDto;
import com.assignment.shop.exceptions.ResourceNotFoundException;
import com.assignment.shop.orders.dto.OrderDto;
import com.assignment.shop.orders.dto.OrderItemDto;
import com.assignment.shop.orders.entity.Order;
import com.assignment.shop.orders.repo.OrderRepository;
import com.assignment.shop.products.entity.Product;
import com.assignment.shop.products.repo.ProductRepository;
import com.assignment.shop.users.entity.User;
import com.assignment.shop.users.enums.Role;
import com.assignment.shop.users.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private ProductRepository productRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private DiscountStrategy discountEngine;

    @InjectMocks
    private OrderService orderService;

    private User regularUser;
    private User premiumUser;
    private Product laptopProduct;
    private Product mouseProduct;
    private OrderItemDto orderItem1;
    private OrderItemDto orderItem2;

    @BeforeEach
    void setup() {
        regularUser = User.builder()
                .id(1L)
                .username("john")
                .email("john@test.com")
                .password("encoded_pass")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        premiumUser = User.builder()
                .id(2L)
                .username("jane")
                .email("jane@test.com")
                .password("encoded_pass")
                .role(Role.PREMIUM_USER)
                .createdAt(LocalDateTime.now())
                .build();

        laptopProduct = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("1500.00"))
                .quantity(10)
                .status("ACTIVE")
                .build();

        mouseProduct = Product.builder()
                .id(2L)
                .name("Mouse")
                .price(new BigDecimal("50.00"))
                .quantity(20)
                .status("ACTIVE")
                .build();

        orderItem1 = OrderItemDto.builder()
                .productId(1L)
                .quantity(2)
                .build();

        orderItem2 = OrderItemDto.builder()
                .productId(2L)
                .quantity(1)
                .build();
    }

    @Test
    @DisplayName("Should place order for regular user without discount")
    void whenRegularUserPlacesOrder_thenNoDiscount() {
        List<OrderItemDto> items = List.of(orderItem1);
        when(userRepo.findById(1L)).thenReturn(Optional.of(regularUser));
        when(productRepo.findById(1L)).thenReturn(Optional.of(laptopProduct));

        BigDecimal subtotal = new BigDecimal("3000.00");
        DiscountReportDto discountReport = DiscountReportDto.builder()
                .totalDiscount(BigDecimal.ZERO)
                .finalAmount(subtotal)
                .build();
        when(discountEngine.evaluate(any(User.class), any(BigDecimal.class))).thenReturn(discountReport);
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderDto result = orderService.placeOrder(1L, items);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    @DisplayName("Should apply discount for premium user")
    void whenPremiumUserPlacesOrder_thenApplyDiscount() {
        List<OrderItemDto> items = List.of(orderItem1);
        when(userRepo.findById(2L)).thenReturn(Optional.of(premiumUser));
        when(productRepo.findById(1L)).thenReturn(Optional.of(laptopProduct));

        BigDecimal subtotal = new BigDecimal("3000.00");
        BigDecimal discount = new BigDecimal("300.00");
        DiscountReportDto discountReport = DiscountReportDto.builder()
                .totalDiscount(discount)
                .finalAmount(subtotal.subtract(discount))
                .build();
        when(discountEngine.evaluate(any(User.class), any(BigDecimal.class))).thenReturn(discountReport);
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderDto result = orderService.placeOrder(2L, items);

        assertThat(result).isNotNull();
        assertThat(result.getDiscountAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getDiscountAmount()).isEqualTo(discount);
    }

    @Test
    @DisplayName("Should throw exception when product out of stock")
    void whenProductOutOfStock_thenThrowException() {
        laptopProduct.setQuantity(0);
        when(userRepo.findById(1L)).thenReturn(Optional.of(regularUser));
        when(productRepo.findById(1L)).thenReturn(Optional.of(laptopProduct));

        assertThatThrownBy(() -> orderService.placeOrder(1L, List.of(orderItem1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void whenProductNotFound_thenThrowException() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(regularUser));
        when(productRepo.findById(999L)).thenReturn(Optional.empty());

        OrderItemDto invalidItem = OrderItemDto.builder()
                .productId(999L)
                .quantity(1)
                .build();

        assertThatThrownBy(() -> orderService.placeOrder(1L, List.of(invalidItem)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void whenUserNotFound_thenThrowException() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(999L, List.of(orderItem1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should reduce product quantity after order")
    void whenOrderPlaced_thenReduceProductQuantity() {
        int initialQuantity = laptopProduct.getQuantity();
        when(userRepo.findById(1L)).thenReturn(Optional.of(regularUser));
        when(productRepo.findById(1L)).thenReturn(Optional.of(laptopProduct));

        DiscountReportDto discountReport = DiscountReportDto.builder()
                .totalDiscount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("3000.00"))
                .build();
        when(discountEngine.evaluate(any(User.class), any(BigDecimal.class))).thenReturn(discountReport);
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        orderService.placeOrder(1L, List.of(orderItem1));

        verify(productRepo).save(argThat(product ->
            product.getQuantity() == initialQuantity - orderItem1.getQuantity()
        ));
    }

    @Test
    @DisplayName("Multiple items order")
    void whenOrderWithMultipleItems_thenProcessAll() {
        List<OrderItemDto> multipleItems = Arrays.asList(orderItem1, orderItem2);
        when(userRepo.findById(1L)).thenReturn(Optional.of(regularUser));
        when(productRepo.findById(1L)).thenReturn(Optional.of(laptopProduct));
        when(productRepo.findById(2L)).thenReturn(Optional.of(mouseProduct));

        DiscountReportDto discountReport = DiscountReportDto.builder()
                .totalDiscount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("3050.00"))
                .build();
        when(discountEngine.evaluate(any(User.class), any(BigDecimal.class))).thenReturn(discountReport);
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderDto result = orderService.placeOrder(1L, multipleItems);

        assertThat(result).isNotNull();
        verify(productRepo, times(2)).findById(anyLong());
        verify(productRepo, times(2)).save(any(Product.class));
    }
}

