package com.grocerypos.customer.entity;

import com.grocerypos.core.base.BaseEntity;
import lombok.*;

/**
 * Thực thể Lịch sử công nợ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DebtRecord extends BaseEntity {
    private Long customerId;
    private Long orderId;         // null nếu trả nợ thủ công
    private DebtType type;        // BORROW | REPAY
    private double amount;        // luôn dương
    private String note;
}
