package com.grocerypos.core.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.core.session.UserRole;
import com.grocerypos.core.session.UserSession;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository truy vấn bảng users để lấy thông tin đăng nhập.
 */
public class AuthRepository extends BaseRepository {

    /**
     * Tìm kiếm thông tin user theo tên đăng nhập.
     */
    public Optional<UserSessionWithPassword> findByUsername(String username) {
        String sql = "SELECT id, username, password, display_name, role FROM users WHERE username = ? AND is_active = 1";
        return queryOne(sql, rs -> {
            UserRole role = UserRole.valueOf(rs.getString("role"));
            UserSession session = UserSession.builder()
                    .userId(rs.getLong("id"))
                    .username(rs.getString("username"))
                    .displayName(rs.getString("display_name"))
                    .role(role)
                    .loginTime(LocalDateTime.now())
                    .build();
            return new UserSessionWithPassword(session, rs.getString("password"));
        }, username);
    }

    public void updatePassword(Long userId, String hashedNewPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        update(sql, hashedNewPassword, userId);
    }

    /**
     * Lớp DTO nội bộ để trả về cả session và mật khẩu đã hash để so khớp.
     */
    public record UserSessionWithPassword(UserSession session, String hashedPassword) {}
}
