package com.grocerypos.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Tiện ích quản lý hình ảnh sản phẩm.
 */
public class ImageUtils {
    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);
    private static final String IMAGE_DIR = System.getProperty("user.home") + "/.config/GroceryPOS/data/images/products/";

    static {
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Lưu ảnh vào thư mục hệ thống.
     * @param sourceFile File nguồn người dùng chọn
     * @param fileName Tên file mới (thường là mã sản phẩm)
     * @return Đường dẫn tương đối hoặc tuyệt đối để lưu vào DB
     */
    public static String saveProductImage(File sourceFile, String fileName) {
        try {
            String extension = getFileExtension(sourceFile.getName());
            String newFileName = fileName + extension;
            File destFile = new File(IMAGE_DIR + newFileName);
            
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Đã lưu ảnh sản phẩm tại: {}", destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            log.error("Lỗi khi lưu ảnh sản phẩm", e);
            return null;
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? ".png" : fileName.substring(lastDot);
    }
}
