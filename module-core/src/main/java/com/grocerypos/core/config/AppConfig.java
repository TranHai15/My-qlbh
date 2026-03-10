package com.grocerypos.core.config;

import com.grocerypos.core.exception.AppException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lớp tiện ích đọc cấu hình từ file application.properties.
 */
public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (in == null) {
                throw new AppException("Không tìm thấy file application.properties trong resources");
            }
            props.load(in);
        } catch (IOException e) {
            throw new AppException("Lỗi khi tải application.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key, "");
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) return defaultValue;
        return Integer.parseInt(value);
    }
}
