package com.grocerypos.order.entity;

import com.grocerypos.core.base.BaseEntity;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {
    private Long orderId;
    private String method; // 'CASH' hoặc 'DEBT'
    private double amountPaid;
    private double changeAmount;
    private boolean isDebt; // 1 = khách nợ
}
