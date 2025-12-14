package com.assignment.shop.validation;

import com.assignment.shop.orders.dto.OrderDto;
import com.assignment.shop.orders.dto.OrderItemDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderDto Validation Tests")
class OrderDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid order should pass validation")
    void whenValidOrder_thenNoViolations() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(2)
                .build();

        OrderDto order = OrderDto.builder()
                .items(List.of(item))
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);

        assertTrue(violations.isEmpty(), "Valid order should have no violations");
    }

    @Test
    @DisplayName("Order items are required")
    void whenItemsIsNull_thenViolation() {
        OrderDto order = OrderDto.builder()
                .items(null)
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Order items are required")));
    }

    @Test
    @DisplayName("Order must contain at least one item")
    void whenItemsIsEmpty_thenViolation() {
        OrderDto order = OrderDto.builder()
                .items(Collections.emptyList())
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("at least one item")));
    }

    @Test
    @DisplayName("Order cannot contain more than 100 items")
    void whenTooManyItems_thenViolation() {
        List<OrderItemDto> items = IntStream.rangeClosed(1, 101)
                .mapToObj(i -> OrderItemDto.builder()
                        .productId((long) i)
                        .quantity(1)
                        .build())
                .collect(Collectors.toList());

        OrderDto order = OrderDto.builder()
                .items(items)
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("100 items")));
    }

    @Test
    @DisplayName("Nested validation: Invalid order item should cause violation")
    void whenOrderItemInvalid_thenViolation() {
        OrderItemDto invalidItem = OrderItemDto.builder()
                .productId(null)  // Invalid: required
                .quantity(0)       // Invalid: must be at least 1
                .build();

        OrderDto order = OrderDto.builder()
                .items(List.of(invalidItem))
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);

        assertFalse(violations.isEmpty(), "Invalid nested item should cause violations");
        assertTrue(violations.size() >= 2, "Should have violations for productId and quantity");
    }

    @Test
    @DisplayName("Multiple items: one invalid should cause violation")
    void whenOneItemInvalid_thenViolation() {
        OrderItemDto validItem = OrderItemDto.builder()
                .productId(1L)
                .quantity(2)
                .build();

        OrderItemDto invalidItem = OrderItemDto.builder()
                .productId(-1L)  // Invalid: must be positive
                .quantity(0)      // Invalid: must be at least 1
                .build();

        OrderDto order = OrderDto.builder()
                .items(List.of(validItem, invalidItem))
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Order with exactly 100 items should pass")
    void whenExactly100Items_thenNoViolation() {
        List<OrderItemDto> items = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> OrderItemDto.builder()
                        .productId((long) i)
                        .quantity(1)
                        .build())
                .collect(Collectors.toList());

        OrderDto order = OrderDto.builder()
                .items(items)
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(order);

        assertTrue(violations.isEmpty(), "100 items should be valid");
    }
}

