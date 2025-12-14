package com.assignment.shop.validation;

import com.assignment.shop.orders.dto.OrderItemDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderItemDto Validation Tests")
class OrderItemDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid order item should pass validation")
    void whenValidOrderItem_thenNoViolations() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(5)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertTrue(violations.isEmpty(), "Valid order item should have no violations");
    }

    @Test
    @DisplayName("Product ID is required")
    void whenProductIdIsNull_thenViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(null)
                .quantity(5)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Product ID is required")));
    }

    @Test
    @DisplayName("Product ID must be positive")
    void whenProductIdIsZero_thenViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(0L)
                .quantity(5)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must be positive")));
    }

    @Test
    @DisplayName("Product ID cannot be negative")
    void whenProductIdIsNegative_thenViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(-5L)
                .quantity(5)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must be positive")));
    }

    @Test
    @DisplayName("Quantity is required")
    void whenQuantityIsNull_thenViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(null)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Quantity is required")));
    }

    @Test
    @DisplayName("Quantity must be at least 1")
    void whenQuantityIsZero_thenViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(0)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("at least 1")));
    }

    @Test
    @DisplayName("Quantity cannot be negative")
    void whenQuantityIsNegative_thenViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(-3)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("at least 1")));
    }

    @Test
    @DisplayName("Quantity cannot exceed 1000")
    void whenQuantityTooHigh_thenViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(1001)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("1000")));
    }

    @Test
    @DisplayName("Quantity of exactly 1000 should be valid")
    void whenQuantityIs1000_thenNoViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(1000)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertTrue(violations.isEmpty(), "Quantity of 1000 should be valid");
    }

    @Test
    @DisplayName("Quantity of 1 should be valid")
    void whenQuantityIs1_thenNoViolation() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(1)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(item);

        assertTrue(violations.isEmpty(), "Quantity of 1 should be valid");
    }
}

