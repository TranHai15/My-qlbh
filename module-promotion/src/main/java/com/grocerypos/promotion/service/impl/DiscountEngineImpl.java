package com.grocerypos.promotion.service.impl;

import com.grocerypos.customer.entity.Customer;
import com.grocerypos.order.model.Cart;
import com.grocerypos.promotion.model.DiscountResult;
import com.grocerypos.promotion.service.DiscountEngine;

import java.util.ArrayList;
import java.util.List;

public class DiscountEngineImpl implements DiscountEngine {

    @Override
    public DiscountResult calculate(Cart cart, Customer customer) {
        double subtotal = cart.getSubtotal();
        double bestDiscount = 0;
        List<String> appliedNames = new ArrayList<>();

        // TODO: Mở rộng thêm logic duyệt các Promotion từ Database (PERCENT, BUY_X_GET_Y...) ở đây

        // Tính chiết khấu khách quen
        if (customer != null && customer.getDiscountRate() > 0) {
            double customerDiscount = subtotal * customer.getDiscountRate();
            bestDiscount += customerDiscount; // Tạm thời cộng dồn chiết khấu
            appliedNames.add(String.format("Chiết khấu khách quen (%d%%)", (int)(customer.getDiscountRate() * 100)));
        }

        return new DiscountResult(bestDiscount, appliedNames);
    }
}
