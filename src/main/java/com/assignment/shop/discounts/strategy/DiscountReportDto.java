package com.assignment.shop.discounts.strategy;

import com.assignment.shop.discounts.DiscountResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountReportDto {
    private BigDecimal subtotal;
    private List<DiscountResult> appliedRules;
    private BigDecimal totalDiscount;
    private BigDecimal finalAmount;
}
