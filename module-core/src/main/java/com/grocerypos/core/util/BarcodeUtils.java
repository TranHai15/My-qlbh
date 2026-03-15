package com.grocerypos.core.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

/**
 * Tiện ích tạo mã QR cho sản phẩm.
 */
public class BarcodeUtils {
    private static final Logger log = LoggerFactory.getLogger(BarcodeUtils.class);
    private static final String QR_DIR = System.getProperty("user.home") + "/.config/GroceryPOS/data/qr/";

    static {
        File dir = new File(QR_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Tạo mã QR từ nội dung và lưu vào thư mục cấu hình.
     * @param content Nội dung (thường là barcode)
     * @param fileName Tên file ảnh lưu trữ
     * @return Đường dẫn tuyệt đối của file ảnh QR
     */
    public static String generateQRCode(String content, String fileName) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

            Path path = FileSystems.getDefault().getPath(QR_DIR + fileName + ".png");
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            
            log.info("Đã tạo mã QR tại: {}", path);
            return path.toString();
        } catch (Exception e) {
            log.error("Lỗi khi tạo mã QR", e);
            return null;
        }
    }

    /**
     * Tự động tạo mã vạch ngẫu nhiên nếu không có sẵn.
     */
    public static String generateRandomBarcode() {
        return "893" + (long) (Math.random() * 1000000000L);
    }
}
