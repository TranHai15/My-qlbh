package com.grocerypos.product.entity;

import com.grocerypos.core.base.BaseEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {
    private String name;
    private String description;
    private Long parentId;
    private String imageUrl;
    private boolean isActive;

    // Trường bổ sung để hiển thị (không lưu DB)
    private String parentName;
}
