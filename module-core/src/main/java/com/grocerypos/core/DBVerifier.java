package com.grocerypos.core;

import com.grocerypos.core.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBVerifier {
    public static void main(String[] args) {
        System.out.println("--- KIỂM TRA DỮ LIỆU ĐƠN HÀNG ---");
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("\n1. Danh sách đơn hàng (Orders):");
            ResultSet rsOrders = stmt.executeQuery("SELECT id, order_code, total_amount FROM orders");
            while (rsOrders.next()) {
                System.out.printf("ID: %d | Mã: %s | Tổng: %.2f\n", 
                    rsOrders.getLong("id"), rsOrders.getString("order_code"), rsOrders.getDouble("total_amount"));
            }

            System.out.println("\n2. Chi tiết mặt hàng (Order Items) gắn với Order ID:");
            ResultSet rsItems = stmt.executeQuery(
                "SELECT oi.order_id, o.order_code, oi.product_name, oi.quantity, oi.line_total " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.id");
            
            boolean hasItems = false;
            while (rsItems.next()) {
                hasItems = true;
                System.out.printf("Order ID: %d (%s) | Sản phẩm: %s | SL: %.1f | Thành tiền: %.2f\n",
                    rsItems.getLong("order_id"), rsItems.getString("order_code"), 
                    rsItems.getString("product_name"), rsItems.getDouble("quantity"), rsItems.getDouble("line_total"));
            }
            
            if (!hasItems) {
                System.out.println("=> CẢNH BÁO: Không tìm thấy dữ liệu trong bảng order_items hoặc liên kết bị sai!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseConfig.shutdown();
        }
    }
}
