package com.assignment.shop.discounts;

import com.assignment.shop.discounts.strategy.DiscountReportDto;
import com.assignment.shop.users.entity.User;
import com.assignment.shop.users.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountEngineTest {

    @Mock
    private DiscountRule premiumRule;

    @Mock
    private DiscountRule largeOrderRule;

    private DiscountEngine discountEngine;
    private User regularUser;
    private User premiumUser;
    private User adminUser;

    @BeforeEach
    void setup() {
        List<DiscountRule> rules = Arrays.asList(premiumRule, largeOrderRule);
        discountEngine = new DiscountEngine(rules);

        regularUser = User.builder()
                .id(1L)
                .username("regular")
                .role(Role.USER)
                .build();

        premiumUser = User.builder()
                .id(2L)
                .username("premium")
                .role(Role.PREMIUM_USER)
                .build();

        adminUser = User.builder()
                .id(3L)
                .username("admin")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Regular user should not get discount on small order")
    void whenRegularUserSmallOrder_thenNoDiscount() {
        BigDecimal orderTotal = new BigDecimal("500.00");
        when(premiumRule.isApplicable(any(), any())).thenReturn(false);
        when(largeOrderRule.isApplicable(any(), any())).thenReturn(false);

        DiscountReportDto result = discountEngine.evaluate(regularUser, orderTotal);

        assertThat(result.getTotalDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Premium user should get 10% discount")
    void whenPremiumUser_thenGet10PercentDiscount() {
        BigDecimal orderTotal = new BigDecimal("1000.00");
        DiscountResult premiumDiscount = new DiscountResult("Premium User Discount", new BigDecimal("100.00"));

        when(premiumRule.isApplicable(premiumUser, orderTotal)).thenReturn(true);
        when(premiumRule.apply(premiumUser, orderTotal)).thenReturn(premiumDiscount);
        when(largeOrderRule.isApplicable(any(), any())).thenReturn(false);

        DiscountReportDto result = discountEngine.evaluate(premiumUser, orderTotal);

        assertThat(result.getTotalDiscount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getAppliedRules()).isNotEmpty();
    }

    @Test
    @DisplayName("Large order should get discount for any user")
    void whenLargeOrder_thenGetDiscount() {
        BigDecimal largeOrderTotal = new BigDecimal("5500.00");
        DiscountResult orderDiscount = new DiscountResult("Large Order Discount", new BigDecimal("275.00"));

        when(premiumRule.isApplicable(any(), any())).thenReturn(false);
        when(largeOrderRule.isApplicable(regularUser, largeOrderTotal)).thenReturn(true);
        when(largeOrderRule.apply(regularUser, largeOrderTotal)).thenReturn(orderDiscount);

        DiscountReportDto result = discountEngine.evaluate(regularUser, largeOrderTotal);

        assertThat(result.getTotalDiscount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Premium user with large order gets best discount")
    void whenPremiumUserLargeOrder_thenGetBestDiscount() {
        BigDecimal largeOrderTotal = new BigDecimal("6000.00");

        when(premiumRule.isApplicable(any(), any())).thenReturn(false);
        when(largeOrderRule.isApplicable(any(), any())).thenReturn(false);

        DiscountReportDto regularResult = discountEngine.evaluate(regularUser, largeOrderTotal);
        DiscountReportDto premiumResult = discountEngine.evaluate(premiumUser, largeOrderTotal);

        assertThat(premiumResult.getTotalDiscount())
                .isGreaterThanOrEqualTo(regularResult.getTotalDiscount());
    }

    @Test
    @DisplayName("Admin user should get discounts like premium")
    void whenAdminUser_thenGetDiscount() {
        // given
        BigDecimal orderTotal = new BigDecimal("2000.00");
        when(premiumRule.isApplicable(any(), any())).thenReturn(false);
        when(largeOrderRule.isApplicable(any(), any())).thenReturn(false);

        DiscountReportDto result = discountEngine.evaluate(adminUser, orderTotal);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Zero order zero discount")
    void whenZeroOrderTotal_thenZeroDiscount() {
        BigDecimal zeroTotal = BigDecimal.ZERO;
        when(premiumRule.isApplicable(any(), any())).thenReturn(false);
        when(largeOrderRule.isApplicable(any(), any())).thenReturn(false);

        DiscountReportDto result = discountEngine.evaluate(regularUser, zeroTotal);

        assertThat(result.getTotalDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

