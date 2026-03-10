package com.grocerypos.core;

import com.grocerypos.core.config.DatabaseConfig;
import com.grocerypos.core.config.DatabaseInitializer;
import com.grocerypos.core.repository.AuthRepository;
import com.grocerypos.core.service.AuthService;
import com.grocerypos.core.service.impl.AuthServiceImpl;
import com.grocerypos.core.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Một lớp nhỏ để chạy thử chức năng đăng nhập từ CLI.
 */
public class LoginTester {
    private static final Logger log = LoggerFactory.getLogger(LoginTester.class);

    public static void main(String[] args) {
        try {
            // 1. Khởi tạo DB
            DatabaseInitializer.runSchema();

            // 2. Chuẩn bị Service
            AuthRepository authRepo = new AuthRepository();
            AuthService authService = new AuthServiceImpl(authRepo);

            // 3. Chạy thử
            log.info("--- THỬ ĐĂNG NHẬP ---");
            UserSession session = authService.login("admin", "admin123");
            
            log.info("ĐĂNG NHẬP THÀNH CÔNG!");
            log.info("Người dùng: {}", session.getDisplayName());
            log.info("Quyền hạn: {}", session.getRole());
            
        } catch (Exception e) {
            log.error("ĐĂNG NHẬP THẤT BẠI: {}", e.getMessage());
        } finally {
            DatabaseConfig.shutdown();
        }
    }
}
