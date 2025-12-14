package com.assignment.shop.validation;

import com.assignment.shop.products.dto.ProductDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductDto Validation Tests")
class ProductDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid product should pass validation")
    void whenValidProduct_thenNoViolations() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .description("Latest iPhone model")
                .price(new BigDecimal("999.99"))
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertTrue(violations.isEmpty(), "Valid product should have no violations");
    }

    @Test
    @DisplayName("Product name is required")
    void whenNameIsNull_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name(null)
                .price(new BigDecimal("999.99"))
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertEquals(1, violations.size());
        assertEquals("Product name is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Product name cannot be blank")
    void whenNameIsBlank_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name("   ")
                .price(new BigDecimal("999.99"))
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Product name is required")));
    }

    @Test
    @DisplayName("Product name must be at least 3 characters")
    void whenNameTooShort_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name("AB")
                .price(new BigDecimal("999.99"))
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 100 characters")));
    }

    @Test
    @DisplayName("Product name cannot exceed 100 characters")
    void whenNameTooLong_thenViolation() {
        String longName = "A".repeat(101);
        ProductDto product = ProductDto.builder()
                .name(longName)
                .price(new BigDecimal("999.99"))
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 100 characters")));
    }

    @Test
    @DisplayName("Price is required")
    void whenPriceIsNull_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .price(null)
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Price is required")));
    }

    @Test
    @DisplayName("Price must be greater than 0")
    void whenPriceIsZero_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .price(BigDecimal.ZERO)
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("greater than 0")));
    }

    @Test
    @DisplayName("Price cannot be negative")
    void whenPriceIsNegative_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .price(new BigDecimal("-100"))
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("greater than 0")));
    }

    @Test
    @DisplayName("Price must have valid decimal format")
    void whenPriceHasInvalidFormat_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .price(new BigDecimal("12345678901.123")) // 11 integer digits
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("maximum 10 integer digits")));
    }

    @Test
    @DisplayName("Quantity is required")
    void whenQuantityIsNull_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .price(new BigDecimal("999.99"))
                .quantity(null)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Quantity is required")));
    }

    @Test
    @DisplayName("Quantity cannot be negative")
    void whenQuantityIsNegative_thenViolation() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .price(new BigDecimal("999.99"))
                .quantity(-5)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("cannot be negative")));
    }

    @Test
    @DisplayName("Quantity can be zero (out of stock)")
    void whenQuantityIsZero_thenNoViolation() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .price(new BigDecimal("999.99"))
                .quantity(0)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertTrue(violations.isEmpty(), "Quantity can be 0 for out-of-stock products");
    }

    @Test
    @DisplayName("Description is optional")
    void whenDescriptionIsNull_thenNoViolation() {
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .description(null)
                .price(new BigDecimal("999.99"))
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertTrue(violations.isEmpty(), "Description is optional");
    }

    @Test
    @DisplayName("Description cannot exceed 500 characters")
    void whenDescriptionTooLong_thenViolation() {
        String longDescription = "A".repeat(501);
        ProductDto product = ProductDto.builder()
                .name("iPhone 15 Pro")
                .description(longDescription)
                .price(new BigDecimal("999.99"))
                .quantity(50)
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("500 characters")));
    }

    @Test
    @DisplayName("Multiple validation errors should be reported")
    void whenMultipleFieldsInvalid_thenMultipleViolations() {
        ProductDto product = ProductDto.builder()
                .name("AB")  // Too short
                .price(new BigDecimal("-100"))  // Negative
                .quantity(-5)  // Negative
                .build();

        Set<ConstraintViolation<ProductDto>> violations = validator.validate(product);

        assertTrue(violations.size() >= 3, "Should have at least 3 violations");
    }
}

