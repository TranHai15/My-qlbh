package com.grocerypos.order.service;

import com.grocerypos.order.entity.Order;
import com.grocerypos.order.entity.OrderItem;
import com.grocerypos.order.model.Cart;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(Cart cart, Long customerId, double discountAmount, double pointsToUse);
    
    // Gộp quy trình tạo đơn và thanh toán vào 1 transaction
    Order checkout(Cart cart, Long customerId, double discountAmount, double pointsToUse, double amountPaid);
    
    Optional<Order> findById(Long id);
    Optional<Order> findByCode(String code);
    List<Order> findAll();
    
    // New methods for management
    List<OrderItem> getOrderItems(Long orderId);
    List<Order> searchOrders(String query, LocalDate date);
}
