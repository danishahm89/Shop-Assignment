package com.assignment.shop.discounts.strategy;

import com.assignment.shop.constants.AppConstants;
import com.assignment.shop.discounts.DiscountResult;
import com.assignment.shop.discounts.DiscountRule;
import com.assignment.shop.users.entity.User;
import com.assignment.shop.users.enums.Role;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static java.util.Objects.nonNull;

@Component
public class PremiumUserDiscountRule implements DiscountRule {

    @Override
    public int getPriority(){
        return 100;
    }

    @Override
    public boolean isApplicable(User user, BigDecimal subtotal) {
        return nonNull(user) && Role.PREMIUM_USER == user.getRole();
    }

    @Override
    public DiscountResult apply(User user, BigDecimal subtotal) {
        BigDecimal discountAmount = subtotal.multiply(new BigDecimal(AppConstants.PREMIUM_DISCOUNT_RATE));
        return new DiscountResult(AppConstants.DISCOUNT_PREMIUM_USER, discountAmount);
    }
}
