package com.grocerypos.order.service;

import com.grocerypos.order.entity.Payment;

public interface PaymentService {
    Payment processPayment(Long orderId, double amountPaid, Long customerId);
    double calculateChange(double totalAmount, double amountPaid);
}
