package com.grocerypos.promotion.service;

import com.grocerypos.customer.entity.Customer;
import com.grocerypos.order.model.Cart;
import com.grocerypos.promotion.model.DiscountResult;

public interface DiscountEngine {
    DiscountResult calculate(Cart cart, Customer customer);
}
