package com.grocerypos.order.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.order.entity.Order;
import com.grocerypos.order.entity.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository extends BaseRepository {
    private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);

    public Order save(Order order) {
        String sql = "INSERT INTO orders (order_code, customer_id, subtotal, discount_amount, total_amount, status, notes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        long id = insert(sql,
                order.getOrderCode(),
                order.getCustomerId(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getNotes(),
                order.getCreatedAt().toString()
        );
        order.setId(id);
        return order;
    }

    public int countToday() {
        String today = LocalDate.now().toString();
        String sql = "SELECT COUNT(*) as count FROM orders WHERE DATE(created_at) = ?";
        return queryOne(sql, rs -> rs.getInt("count"), today).orElse(0);
    }

    public Optional<Order> findById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        return queryOne(sql, this::mapRow, id);
    }

    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY created_at DESC LIMIT 100";
        return queryList(sql, this::mapRow);
    }

    public List<Order> search(String query, LocalDate date) {
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append(" AND order_code LIKE ?");
            params.add("%" + query + "%");
        }
        if (date != null) {
            sql.append(" AND DATE(created_at) = ?");
            params.add(date.toString());
        }
        sql.append(" ORDER BY created_at DESC");

        return queryList(sql.toString(), this::mapRow, params.toArray());
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = Order.builder()
                .orderCode(rs.getString("order_code"))
                .subtotal(rs.getDouble("subtotal"))
                .discountAmount(rs.getDouble("discount_amount"))
                .totalAmount(rs.getDouble("total_amount"))
                .status(OrderStatus.valueOf(rs.getString("status")))
                .notes(rs.getString("notes"))
                .build();
        order.setId(rs.getLong("id"));
        
        long customerId = rs.getLong("customer_id");
        if (!rs.wasNull()) {
            order.setCustomerId(customerId);
        }
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try {
                // SQLite datetime format: YYYY-MM-DD HH:MM:SS
                order.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
            } catch (Exception e) {
                log.warn("Lỗi parse created_at: {}", createdAt);
            }
        }
        
        return order;
    }
}
