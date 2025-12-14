package com.assignment.shop.discounts;

import com.assignment.shop.users.entity.User;

import java.math.BigDecimal;

public interface DiscountRule {
    int getPriority();
    boolean isApplicable(User user, BigDecimal subtotal);
    DiscountResult apply(User user, BigDecimal subtotal);
}
