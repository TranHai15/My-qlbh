package com.grocerypos.core.service;

import com.grocerypos.core.session.UserSession;
import java.util.Optional;

/**
 * Interface cho dịch vụ xác thực người dùng.
 */
public interface AuthService {
    /**
     * Thực hiện đăng nhập vào hệ thống.
     * @param username Tên đăng nhập
     * @param password Mật khẩu chưa mã hóa
     * @return UserSession nếu đăng nhập thành công
     * @throws com.grocerypos.core.exception.ValidationException nếu thông tin sai
     */
    UserSession login(String username, String password);

    /**
     * Đăng xuất khỏi hệ thống.
     */
    void logout();

    /**
     * Thay đổi mật khẩu quản trị.
     */
    void changeAdminPassword(String oldPassword, String newPassword);
}
