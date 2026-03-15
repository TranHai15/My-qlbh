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
        
        // Tìm file seed_data.sql thông minh hơn
        java.io.File sqlFile = new java.io.File("module-core/seed_data.sql");
        if (!sqlFile.exists()) {
            sqlFile = new java.io.File("seed_data.sql"); // Truong hop chay tu module-core
        }
        
        System.out.println("Dang nap du lieu tu: " + sqlFile.getAbsolutePath());
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (!sqlFile.exists()) {
                throw new java.io.FileNotFoundException("Khong tim thay file seed_data.sql! Hay dam bao ban dang o thu muc goc cua du an.");
            }
            String sql = Files.readString(sqlFile.toPath());
            String[] statements = sql.split(";");
            
            Statement stmt = conn.createStatement();
            for (String s : statements) {
                if (!s.trim().isEmpty()) {
                    String cmd = s.trim();
                    // Tu dong chuyen INSERT thanh INSERT OR IGNORE de tranh loi trung lap khi chay lai setup
                    if (cmd.toUpperCase().startsWith("INSERT INTO")) {
                        cmd = cmd.replaceFirst("(?i)INSERT INTO", "INSERT OR IGNORE INTO");
                    }
                    try {
                        stmt.execute(cmd);
                    } catch (Exception e) {
                        System.err.println("Loi thuc thi cau lenh: " + cmd);
                        // Khong nem loi de tiep tuc cac cau lenh khac
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
