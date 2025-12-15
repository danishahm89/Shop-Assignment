package com.assignment.shop.discounts;

import com.assignment.shop.discounts.strategy.DiscountReportDto;
import com.assignment.shop.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class DiscountEngine implements DiscountStrategy {

    private final List<DiscountRule> rules;

    @Override
    public DiscountReportDto evaluate(User user, BigDecimal subtotal) {
        List<DiscountRule> sorted = rules.stream()
                .sorted(Comparator.comparingInt(DiscountRule::getPriority))
                .toList();

        List<DiscountResult> applied = new ArrayList<>();
        BigDecimal discount = BigDecimal.ZERO;

        for (DiscountRule rule : sorted) {
            if (rule.isApplicable(user, subtotal)) {
                DiscountResult result = rule.apply(user, subtotal);
                if (hasValue(result.getDiscountAmount())) {
                    applied.add(result);
                    discount = discount.add(result.getDiscountAmount());
                }
            }
        }

        discount = discount.min(subtotal);
        BigDecimal finalPrice = subtotal.subtract(discount);

        return DiscountReportDto.builder()
                .subtotal(subtotal)
                .appliedRules(applied)
                .totalDiscount(discount)
                .finalAmount(finalPrice)
                .build();
    }


    private boolean hasValue(BigDecimal amount) {
        return nonNull(amount) && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
