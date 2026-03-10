package com.grocerypos.ui;

import com.grocerypos.core.config.DatabaseConfig;
import com.grocerypos.core.config.DatabaseInitializer;
import com.grocerypos.ui.utils.NavigationHelper;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lớp khởi chạy ứng dụng GroceryPOS (JavaFX).
 */
public class MainApp extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) {
        log.info("Khởi động ứng dụng GroceryPOS...");
        
        try {
            // 1. Khởi tạo Database & Schema
            DatabaseInitializer.runSchema();
            
            // 2. Khởi tạo AppContext (DI)
            AppContext.initialize();
            
            // 3. Thiết lập Stage chính & Navigation
            NavigationHelper.setPrimaryStage(primaryStage);
            primaryStage.setTitle("Camellia - Sắc Hồng Anh Đào");
            
            // 4. Mở màn hình Login
            NavigationHelper.navigateTo("login-view.fxml");
            
            primaryStage.show();
            
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi khởi động ứng dụng", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        log.info("Đang đóng ứng dụng...");
        DatabaseConfig.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
