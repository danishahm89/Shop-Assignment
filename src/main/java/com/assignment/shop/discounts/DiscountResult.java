package com.assignment.shop.discounts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResult {
    private String ruleName;
    private BigDecimal discountAmount;
}
