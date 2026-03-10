package com.grocerypos.ui;

/**
 * Lớp trung gian để khởi chạy ứng dụng JavaFX mà không cần module-info.java.
 * Giúp tránh lỗi NoClassDefFoundError trên Java 11+.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
