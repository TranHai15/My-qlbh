package com.grocerypos.promotion.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DiscountResult {
    private double discountAmount;
    private List<String> appliedPromotions;
}
