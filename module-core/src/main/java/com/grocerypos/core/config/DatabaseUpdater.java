package com.grocerypos.core.config;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseUpdater {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            Statement stmt = conn.createStatement();
            
            // 1. Tạo bảng suppliers (Nếu chưa có)
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "phone TEXT NOT NULL," +
                    "email TEXT," +
                    "address TEXT," +
                    "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))," +
                    "deleted_at TEXT" +
                    ")");
            System.out.println("=> Đã đảm bảo bảng 'suppliers' tồn tại.");

            // 2. Tạo bảng stock_entries (Nếu chưa có)
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "product_id INTEGER NOT NULL," +
                    "supplier_id INTEGER NOT NULL," +
                    "quantity REAL NOT NULL," +
                    "cost_price REAL NOT NULL," +
                    "entry_date TEXT NOT NULL DEFAULT (datetime('now','localtime'))," +
                    "note TEXT," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)," +
                    "FOREIGN KEY (supplier_id) REFERENCES suppliers(id)" +
                    ")");
            System.out.println("=> Đã đảm bảo bảng 'stock_entries' tồn tại.");

            // 3. Tạo bảng orders
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "order_code TEXT NOT NULL UNIQUE," +
                    "customer_id INTEGER," +
                    "subtotal REAL NOT NULL," +
                    "discount_amount REAL NOT NULL DEFAULT 0," +
                    "total_amount REAL NOT NULL," +
                    "status TEXT NOT NULL DEFAULT 'COMPLETED'," +
                    "notes TEXT," +
                    "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))" +
                    ")");

            // 4. Tạo bảng order_items
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "order_id INTEGER NOT NULL," +
                    "product_id INTEGER NOT NULL," +
                    "product_name TEXT NOT NULL," +
                    "unit_price REAL NOT NULL," +
                    "cost_price REAL NOT NULL," +
                    "quantity REAL NOT NULL," +
                    "discount_amount REAL NOT NULL DEFAULT 0," +
                    "line_total REAL NOT NULL," +
                    "FOREIGN KEY (order_id) REFERENCES orders(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")");
            
            // Migration: Thêm discount_amount nếu chưa có (trường hợp bảng đã tồn tại từ trước)
            try {
                stmt.execute("ALTER TABLE order_items ADD COLUMN discount_amount REAL NOT NULL DEFAULT 0");
                System.out.println("=> Đã cập nhật cột 'discount_amount' cho bảng 'order_items'.");
            } catch (Exception e) {
                // Bỏ qua nếu cột đã tồn tại
            }
            
            System.out.println("=> Đã đảm bảo bảng 'orders' và 'order_items' tồn tại.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseConfig.shutdown();
        }
    }
}
