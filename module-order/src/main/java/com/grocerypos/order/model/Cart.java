package com.grocerypos.order.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    private List<CartItem> items = new ArrayList<>();

    public double getSubtotal() {
        return items.stream()
                .mapToDouble(CartItem::getLineTotal)
                .sum();
    }

    public void addItem(CartItem item) {
        items.add(item);
    }
}
