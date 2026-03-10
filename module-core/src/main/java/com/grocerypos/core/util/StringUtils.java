package com.grocerypos.core.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Tiện ích xử lý chuỗi văn bản.
 */
public class StringUtils {
    /**
     * Loại bỏ dấu tiếng Việt khỏi chuỗi.
     */
    public static String removeDiacritics(String input) {
        if (input == null) return null;
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("")
                .replace('đ', 'd').replace('Đ', 'D');
    }

    /**
     * Chuẩn hóa từ khóa tìm kiếm: không dấu, lowercase, trim.
     */
    public static String normalizeSearch(String input) {
        if (input == null) return "";
        return removeDiacritics(input).toLowerCase().trim();
    }

    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}
