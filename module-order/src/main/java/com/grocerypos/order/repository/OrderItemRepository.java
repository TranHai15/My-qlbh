package com.grocerypos.order.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.order.entity.OrderItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class OrderItemRepository extends BaseRepository {

    public OrderItem save(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, product_id, product_name, unit_price, cost_price, quantity, discount_amount, line_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        long id = insert(sql,
                item.getOrderId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getCostPrice(),
                item.getQuantity(),
                item.getDiscountAmount(),
                item.getLineTotal()
        );
        item.setId(id);
        return item;
    }

    public List<OrderItem> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        return queryList(sql, this::mapRow, orderId);
    }

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem item = OrderItem.builder()
                .orderId(rs.getLong("order_id"))
                .productId(rs.getLong("product_id"))
                .productName(rs.getString("product_name"))
                .unitPrice(rs.getDouble("unit_price"))
                .costPrice(rs.getDouble("cost_price"))
                .quantity(rs.getDouble("quantity"))
                .discountAmount(rs.getDouble("discount_amount"))
                .lineTotal(rs.getDouble("line_total"))
                .build();
        item.setId(rs.getLong("id"));
        return item;
    }
}
