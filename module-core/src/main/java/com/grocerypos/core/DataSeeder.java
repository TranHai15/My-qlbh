package com.grocerypos.core;

import com.grocerypos.core.config.DatabaseConfig;
import com.grocerypos.core.config.DatabaseInitializer;
import com.grocerypos.core.config.DatabaseUpdater;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class DataSeeder {
    public static void main(String[] args) {
        System.out.println("Bắt đầu khởi tạo database và nạp dữ liệu giả...");
        
        // 1. Khởi tạo Schema và Update bảng
        DatabaseInitializer.runSchema();
        DatabaseUpdater.main(new String[]{});
        
        // DatabaseUpdater.main có thể gọi shutdown(), cần initialize lại
        DatabaseConfig.initialize();
        
        String projectRoot = System.getProperty("user.dir");
        java.io.File sqlFile = new java.io.File(projectRoot, "seed_data.sql");
        
        System.out.println("Đang tìm file SQL tại: " + sqlFile.getAbsolutePath());
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = Files.readString(sqlFile.toPath());
            String[] statements = sql.split(";");
            
            Statement stmt = conn.createStatement();
            for (String s : statements) {
                if (!s.trim().isEmpty()) {
                    try {
                        stmt.execute(s.trim());
                    } catch (Exception e) {
                        System.err.println("CRITICAL ERROR executing: " + s.trim());
                        throw e; // Rethrow to stop execution
                    }
                }
            }
            System.out.println("=> Hoàn tất nạp dữ liệu giả!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseConfig.shutdown();
        }
    }
}
