package com.grocerypos.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Cấu hình cơ sở dữ liệu SQLite sử dụng HikariCP.
 * Lấy cấu hình từ AppConfig.
 */
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    static {
        initialize();
    }

    public static void initialize() {
        String rawPath = AppConfig.get("db.path", "grocerypos.db");
        String dbPath = resolveDbPath(rawPath);
        
        log.info("Khởi tạo kết nối đến database tại: {}", dbPath);

        // Đảm bảo thư mục cha tồn tại
        File dbFile = new File(dbPath);
        if (dbFile.getParentFile() != null) {
            dbFile.getParentFile().mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbPath);
        config.setMaximumPoolSize(5);
        config.setPoolName("GroceryPOS-Pool");
        
        // SQLite optimization theo SKILL-core.md
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("foreign_keys", "ON");
        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    private static String resolveDbPath(String path) {
        if (path.startsWith("~/")) {
            return System.getProperty("user.home") + path.substring(1);
        }
        
        // Nếu là path tương đối đơn giản, đưa vào thư mục app data theo OS
        if (!path.contains("/") && !path.contains("\\")) {
            String os = System.getProperty("os.name").toLowerCase();
            String userHome = System.getProperty("user.home");
            String appName = "GroceryPOS";
            
            if (os.contains("win")) {
                return System.getenv("APPDATA") + File.separator + appName + File.separator + path;
            } else if (os.contains("mac")) {
                return userHome + "/Library/Application Support/" + appName + "/" + path;
            } else {
                return userHome + "/.config/" + appName + "/" + path;
            }
        }
        return path;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
