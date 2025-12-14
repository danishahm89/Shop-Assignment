package com.assignment.shop.discounts;

import com.assignment.shop.discounts.strategy.DiscountReportDto;
import com.assignment.shop.users.entity.User;

import java.math.BigDecimal;

public interface DiscountStrategy {
    DiscountReportDto evaluate(User user, BigDecimal subtotal);
    BigDecimal applyDiscount(User user, BigDecimal subtotal);

}
