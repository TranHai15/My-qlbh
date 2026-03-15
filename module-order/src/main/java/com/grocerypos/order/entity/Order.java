package com.grocerypos.order.entity;

import com.grocerypos.core.base.BaseEntity;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {
    private String orderCode;       // ORD-20240115-001
    private Long customerId;        // null = khách vãng lai
    private double subtotal;        // trước giảm giá
    private double discountAmount;
    private double totalAmount;     // sau giảm giá
    private OrderStatus status;     // COMPLETED, CANCELLED
    private String notes;

    // Transient
    private transient List<OrderItem> items;
    private transient String customerName;
}
