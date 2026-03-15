package com.grocerypos.order.entity;

import com.grocerypos.core.base.BaseEntity;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private String productName;     // snapshot tên lúc bán
    private double unitPrice;       // snapshot giá lúc bán
    private double costPrice;       // snapshot giá vốn
    private double quantity;
    private double discountAmount;
    private double lineTotal;       // (unitPrice * quantity) - discountAmount
}
