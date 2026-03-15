package com.grocerypos.product.entity;

import com.grocerypos.core.base.BaseEntity;
import lombok.*;

/**
 * Sản phẩm trong hệ thống.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {
    private String name;
    private String barcode;
    private Long categoryId;
    private double costPrice;
    private double sellPrice;
    private double stockQuantity;
    private String imageUrl;
    private String description;
    private boolean isActive;

    // Trường bổ sung để hiển thị (không lưu DB)
    private String categoryName;
}
