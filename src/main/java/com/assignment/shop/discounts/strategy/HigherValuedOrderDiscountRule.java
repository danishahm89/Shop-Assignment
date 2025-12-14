package com.assignment.shop.discounts.strategy;

import com.assignment.shop.constants.AppConstants;
import com.assignment.shop.discounts.DiscountResult;
import com.assignment.shop.discounts.DiscountRule;
import com.assignment.shop.users.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HigherValuedOrderDiscountRule implements DiscountRule {
    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public boolean isApplicable(User user, BigDecimal subtotal) {
        return subtotal.compareTo(new BigDecimal(AppConstants.HIGH_VALUE_ORDER_THRESHOLD)) > 0;
    }

    @Override
    public DiscountResult apply(User user, BigDecimal subtotal) {
        BigDecimal discountAmount = subtotal.multiply(new BigDecimal(AppConstants.HIGH_VALUE_DISCOUNT_RATE));
        return new DiscountResult(AppConstants.DISCOUNT_HIGH_VALUE_ORDER, discountAmount);
    }
}
