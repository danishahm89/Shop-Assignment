package com.assignment.shop.validation;

import com.assignment.shop.users.dto.UserDto;
import com.assignment.shop.users.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDto Validation Tests")
class UserDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid user should pass validation")
    void whenValidUser_thenNoViolations() {
        UserDto user = new UserDto();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Valid user should have no violations");
    }

    @Test
    @DisplayName("Username is required")
    void whenUsernameIsNull_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername(null);
        user.setPassword("password123");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Username is required")));
    }

    @Test
    @DisplayName("Username cannot be blank")
    void whenUsernameIsBlank_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("   ");
        user.setPassword("password123");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Username must be at least 3 characters")
    void whenUsernameTooShort_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("ab");
        user.setPassword("password123");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 50 characters")));
    }

    @Test
    @DisplayName("Username cannot exceed 50 characters")
    void whenUsernameTooLong_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("a".repeat(51));
        user.setPassword("password123");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 3 and 50 characters")));
    }

    @Test
    @DisplayName("Username must contain only alphanumeric and underscore")
    void whenUsernameHasInvalidCharacters_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("john-doe!");
        user.setPassword("password123");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("letters, numbers, and underscores")));
    }

    @Test
    @DisplayName("Valid usernames with underscore should pass")
    void whenUsernameHasUnderscore_thenNoViolation() {
        UserDto user = new UserDto();
        user.setUsername("john_doe_123");
        user.setPassword("password123");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Password is required")
    void whenPasswordIsNull_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("john_doe");
        user.setPassword(null);
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    @DisplayName("Password must be at least 6 characters")
    void whenPasswordTooShort_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("john_doe");
        user.setPassword("12345");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 6 and 100 characters")));
    }

    @Test
    @DisplayName("Email is required")
    void whenEmailIsNull_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setEmail(null);
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email is required")));
    }

    @Test
    @DisplayName("Email must be valid format")
    void whenEmailIsInvalid_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setEmail("invalid-email");
        user.setRole(Role.USER);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }

    @Test
    @DisplayName("Various valid email formats should pass")
    void whenEmailIsValid_thenNoViolation() {
        String[] validEmails = {
                "user@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "user123@test-domain.com"
        };

        for (String email : validEmails) {
            UserDto user = new UserDto();
            user.setUsername("john_doe");
            user.setPassword("password123");
            user.setEmail(email);
            user.setRole(Role.USER);

            Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

            assertTrue(violations.isEmpty(), "Email " + email + " should be valid");
        }
    }

    @Test
    @DisplayName("Role is required")
    void whenRoleIsNull_thenViolation() {
        UserDto user = new UserDto();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setEmail("john@example.com");
        user.setRole(null);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Role is required")));
    }

    @Test
    @DisplayName("All roles should be valid")
    void whenDifferentRoles_thenNoViolation() {
        Role[] roles = {Role.USER, Role.ADMIN, Role.PREMIUM_USER};

        for (Role role : roles) {
            UserDto user = new UserDto();
            user.setUsername("john_doe");
            user.setPassword("password123");
            user.setEmail("john@example.com");
            user.setRole(role);

            Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

            assertTrue(violations.isEmpty(), "Role " + role + " should be valid");
        }
    }

    @Test
    @DisplayName("Multiple validation errors should be reported")
    void whenMultipleFieldsInvalid_thenMultipleViolations() {
        UserDto user = new UserDto();
        user.setUsername("ab");  // Too short
        user.setPassword("123");  // Too short
        user.setEmail("invalid");  // Invalid format
        user.setRole(null);  // Required

        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);

        assertTrue(violations.size() >= 4, "Should have at least 4 violations");
    }
}

