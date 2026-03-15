package com.grocerypos.ui;

import com.grocerypos.core.config.DatabaseInitializer;
import com.grocerypos.ui.controllers.LoginController;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Công cụ chẩn đoán lỗi khởi động ứng dụng mà không cần mở GUI.
 */
public class UIDiagnostic {
    private static final Logger log = LoggerFactory.getLogger(UIDiagnostic.class);

    public static void main(String[] args) {
        // Khởi tạo môi trường JavaFX Toolkit trong chế độ headless
        new JFXPanel(); 

        log.info("=== BẮT ĐẦU CHẨN ĐOÁN HỆ THỐNG ===");
        
        try {
            log.info("1. Kiểm tra Database...");
            DatabaseInitializer.runSchema();
            log.info("=> OK");

            log.info("2. Kiểm tra AppContext...");
            AppContext.initialize();
            log.info("=> OK");

            log.info("3. Kiểm tra nạp FXML (Login)...");
            URL fxmlUrl = UIDiagnostic.class.getResource("/fxml/login-view.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Không tìm thấy file login-view.fxml tại /fxml/");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.load(); // Đây là nơi thường phát sinh lỗi InvocationTargetException
            log.info("=> OK (FXML & Controller loaded)");

            log.info("4. Kiểm tra nạp FXML (Admin Layout)...");
            URL adminUrl = UIDiagnostic.class.getResource("/fxml/admin-layout.fxml");
            FXMLLoader adminLoader = new FXMLLoader(adminUrl);
            adminLoader.load();
            log.info("=> OK");

            log.info("=== CHẨN ĐOÁN HOÀN TẤT: KHÔNG PHÁT HIỆN LỖI CẤU HÌNH ===");
            
        } catch (Exception e) {
            log.error("!!! PHÁT HIỆN LỖI CHI MẠNG !!!");
            log.error("Loại lỗi: {}", e.getClass().getName());
            log.error("Thông điệp: {}", e.getMessage());
            
            if (e.getCause() != null) {
                log.error("Nguyên nhân gốc (Root Cause): {}", e.getCause().getClass().getName());
                log.error("Thông điệp gốc: {}", e.getCause().getMessage());
                e.getCause().printStackTrace(); // In toàn bộ dấu vết lỗi ra console
            } else {
                e.printStackTrace();
            }
        } finally {
            System.exit(0);
        }
    }
}
