package com.grocerypos.customer.entity;

import com.grocerypos.core.base.BaseEntity;
import lombok.*;

/**
 * Thực thể Khách hàng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {
    private String name;          // NOT NULL
    private String phone;         // UNIQUE
    private String address;
    private double discountRate;  // 0.0 - 1.0 (0% - 100%)
    private double rewardPoints;  // Điểm tích lũy hiện tại
    private String notes;
    private boolean active;       // is_active in DB
}
