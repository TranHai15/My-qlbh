package com.grocerypos.order.model;

import com.grocerypos.product.entity.Product;
import lombok.Data;

@Data
public class CartItem {
    private Product product;
    private double quantity;
    private double unitPrice;
    private double itemDiscount;

    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getSellPrice();
        this.itemDiscount = 0;
    }

    public double getLineTotal() {
        return (unitPrice * quantity) - itemDiscount;
    }
}
