package com.grocerypos.core.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Tiện ích xử lý tiền tệ (VNĐ).
 */
public class MoneyUtils {
    private static final Locale VN_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat VN_FORMAT = NumberFormat.getCurrencyInstance(VN_LOCALE);

    public static String formatVND(double amount) {
        // Thay đổi ký hiệu từ mặc định sang "₫" theo yêu cầu
        return VN_FORMAT.format(amount).replace("₫", "₫").replace("VND", "₫");
    }

    public static double round(double amount) {
        return Math.round(amount / 100.0) * 100.0;
    }
}
