package com.grocerypos.core.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích mã hóa mật khẩu sử dụng BCrypt.
 */
public class PasswordUtils {
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String plainText, String hashed) {
        try {
            return BCrypt.checkpw(plainText, hashed);
        } catch (Exception e) {
            return false;
        }
    }
}
