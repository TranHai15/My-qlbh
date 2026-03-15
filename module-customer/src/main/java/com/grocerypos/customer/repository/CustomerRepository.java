package com.grocerypos.customer.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.customer.entity.Customer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CustomerRepository extends BaseRepository {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Customer> findAll() {
        return queryList("SELECT * FROM customers ORDER BY name ASC", this::mapRow);
    }

    public Optional<Customer> findById(Long id) {
        return queryOne("SELECT * FROM customers WHERE id = ?", this::mapRow, id);
    }

    public Optional<Customer> findByPhone(String phone) {
        return queryOne("SELECT * FROM customers WHERE phone = ?", this::mapRow, phone);
    }

    public List<Customer> search(String keyword) {
        String pattern = "%" + keyword + "%";
        return queryList("SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ? ORDER BY name ASC", this::mapRow, pattern, pattern);
    }

    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, address, discount_rate, reward_points, notes, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        long id = insert(sql,
                customer.getName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getDiscountRate(),
                customer.getRewardPoints(),
                customer.getNotes(),
                customer.isActive() ? 1 : 0
        );
        customer.setId(id);
        return customer;
    }

    public Customer update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, address = ?, discount_rate = ?, reward_points = ?, notes = ?, is_active = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        update(sql,
                customer.getName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getDiscountRate(),
                customer.getRewardPoints(),
                customer.getNotes(),
                customer.isActive() ? 1 : 0,
                customer.getId()
        );
        return customer;
    }

    /**
     * Cộng/Trừ điểm tích lũy.
     */
    public void updatePoints(Long id, double delta) {
        update("UPDATE customers SET reward_points = reward_points + ?, updated_at = datetime('now','localtime') WHERE id = ?", delta, id);
    }

    public void delete(Long id) {
        update("UPDATE customers SET is_active = 0 WHERE id = ?", id);
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = Customer.builder()
                .name(rs.getString("name"))
                .phone(rs.getString("phone"))
                .address(rs.getString("address"))
                .discountRate(rs.getDouble("discount_rate"))
                .rewardPoints(rs.getDouble("reward_points"))
                .notes(rs.getString("notes"))
                .active(rs.getInt("is_active") == 1)
                .build();
        c.setId(rs.getLong("id"));
        return c;
    }
}
