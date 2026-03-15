package com.grocerypos.customer.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.customer.entity.DebtRecord;
import com.grocerypos.customer.entity.DebtType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Kho lưu trữ cho lịch sử công nợ.
 */
public class DebtRepository extends BaseRepository {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<DebtRecord> findByCustomerId(Long customerId) {
        String sql = "SELECT * FROM debt_records WHERE customer_id = ? ORDER BY created_at DESC";
        return queryList(sql, this::mapRow, customerId);
    }

    public long save(DebtRecord record) {
        String sql = "INSERT INTO debt_records (customer_id, order_id, type, amount, note) VALUES (?, ?, ?, ?, ?)";
        return insert(sql,
                record.getCustomerId(),
                record.getOrderId(),
                record.getType().name(),
                record.getAmount(),
                record.getNote()
        );
    }

    /**
     * Lưu lịch sử nợ trong một connection (transaction) hiện có.
     */
    public void save(Connection conn, DebtRecord record) throws SQLException {
        String sql = "INSERT INTO debt_records (customer_id, order_id, type, amount, note) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, record.getCustomerId());
            if (record.getOrderId() != null) {
                stmt.setLong(2, record.getOrderId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, record.getType().name());
            stmt.setDouble(4, record.getAmount());
            stmt.setString(5, record.getNote());
            stmt.executeUpdate();
        }
    }

    private DebtRecord mapRow(ResultSet rs) throws SQLException {
        DebtRecord r = DebtRecord.builder()
                .customerId(rs.getLong("customer_id"))
                .orderId(rs.getObject("order_id") != null ? rs.getLong("order_id") : null)
                .type(DebtType.valueOf(rs.getString("type")))
                .amount(rs.getDouble("amount"))
                .note(rs.getString("note"))
                .build();
        r.setId(rs.getLong("id"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            r.setCreatedAt(LocalDateTime.parse(createdAt, ISO_FORMATTER));
        }
        
        return r;
    }
}
