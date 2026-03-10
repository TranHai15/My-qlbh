package com.grocerypos.core.util;

/**
 * Một lớp nhỏ để tạo hash password chuẩn.
 */
public class PasswordHasher {
    public static void main(String[] args) {
        String password = "admin123";
        String hashed = PasswordUtils.hashPassword(password);
        System.out.println("MẬT KHẨU: " + password);
        System.out.println("MÃ BĂM (HASH): " + hashed);
    }
}
