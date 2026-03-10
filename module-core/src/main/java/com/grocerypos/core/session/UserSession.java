package com.grocerypos.core.session;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO lưu trữ thông tin về phiên đăng nhập của người dùng.
 */
@Data
@Builder
public class UserSession {
    private Long userId;
    private String username;
    private String displayName;
    private UserRole role;
    private LocalDateTime loginTime;
}
