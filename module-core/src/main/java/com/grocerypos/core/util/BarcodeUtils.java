package com.grocerypos.core.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;

import java.io.File;
import java.nio.file.Path;

/**
 * Tiện ích tạo và kiểm tra mã vạch (Barcode).
 */
public class BarcodeUtils {
    /**
     * Tạo file ảnh mã vạch EAN-13.
     */
    public static void generateEAN13BarcodeImage(String barcodeText, int width, int height, String filePath) throws Exception {
        EAN13Writer barcodeWriter = new EAN13Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.EAN_13, width, height);
        Path path = new File(filePath).toPath();
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    /**
     * Kiểm tra định dạng mã vạch EAN-13 có hợp lệ không.
     */
    public static boolean isValidEAN13(String barcode) {
        if (barcode == null || !barcode.matches("^\\d{13}$")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checksum = (10 - (sum % 10)) % 10;
        return checksum == Character.getNumericValue(barcode.charAt(12));
    }
}
