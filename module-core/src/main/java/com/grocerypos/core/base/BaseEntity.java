package com.grocerypos.core.base;

import com.grocerypos.core.session.SessionManager;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Lớp cơ sở cho tất cả các thực thể trong hệ thống.
 * Cung cấp các trường thông tin chung như id, ngày tạo, ngày cập nhật.
 */
@Data
public abstract class BaseEntity {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Tự động gán người tạo từ session hiện tại
        this.createdBy = SessionManager.getInstance()
                .getCurrentUsername()
                .orElse("system");
    }
}
