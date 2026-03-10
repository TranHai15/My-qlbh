package com.grocerypos.core.service.impl;

import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.core.repository.AuthRepository;
import com.grocerypos.core.service.AuthService;
import com.grocerypos.core.session.SessionManager;
import com.grocerypos.core.session.UserSession;
import com.grocerypos.core.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation cho dịch vụ xác thực, truy vấn từ bảng users.
 */
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthRepository authRepo;

    public AuthServiceImpl(AuthRepository authRepo) {
        this.authRepo = authRepo;
    }

    @Override
    public UserSession login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ValidationException("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
        }

        AuthRepository.UserSessionWithPassword result = authRepo.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Tên đăng nhập hoặc mật khẩu không chính xác."));

        if (!PasswordUtils.verifyPassword(password, result.hashedPassword())) {
            log.warn("Đăng nhập thất bại cho người dùng: {}", username);
            throw new ValidationException("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        SessionManager.getInstance().login(result.session());
        return result.session();
    }

    @Override
    public void logout() {
        SessionManager.getInstance().logout();
    }

    @Override
    public void changeAdminPassword(String oldPassword, String newPassword) {
        UserSession current = SessionManager.getInstance().getCurrentSession()
                .orElseThrow(() -> new ValidationException("Bạn cần đăng nhập để thực hiện chức năng này."));

        AuthRepository.UserSessionWithPassword result = authRepo.findByUsername(current.getUsername())
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy người dùng hiện tại."));

        if (!PasswordUtils.verifyPassword(oldPassword, result.hashedPassword())) {
            throw new ValidationException("Mật khẩu cũ không chính xác.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        String newHashed = PasswordUtils.hashPassword(newPassword);
        authRepo.updatePassword(current.getUserId(), newHashed);
        log.info("Người dùng '{}' đã đổi mật khẩu thành công.", current.getUsername());
    }
}
