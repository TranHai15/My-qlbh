package com.grocerypos.core.config;

import com.grocerypos.core.base.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Lớp hỗ trợ khởi tạo Database lần đầu tiên.
 */
public class DatabaseInitializer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void runSchema() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            log.info("Bắt đầu khởi tạo schema database...");
            
            // Đọc file schema.sql từ module-desktop-ui resources
            // Lưu ý: Trong thực tế, file này nên nằm ở module-core hoặc được truyền vào
            InputStream is = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql");
            if (is == null) {
                log.warn("Không tìm thấy schema.sql trong resources. Bỏ qua bước khởi tạo bảng.");
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            // Tách các câu lệnh SQL bằng dấu chấm phẩy (đơn giản hóa)
            String[] commands = sql.split(";");
            for (String command : commands) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command);
                }
            }
            
            log.info("Khởi tạo schema database thành công.");
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo schema database", e);
        }
    }
}
