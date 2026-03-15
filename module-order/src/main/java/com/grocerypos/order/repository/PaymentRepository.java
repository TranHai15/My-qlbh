package com.grocerypos.order.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.order.entity.Payment;

import java.sql.Connection;
import java.sql.SQLException;

public class PaymentRepository extends BaseRepository {

    public Payment save(Connection conn, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (order_id, method, amount_paid, change_amount, is_debt, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        long id = insert(conn, sql,
                payment.getOrderId(),
                payment.getMethod(),
                payment.getAmountPaid(),
                payment.getChangeAmount(),
                payment.isDebt() ? 1 : 0,
                payment.getCreatedAt().toString()
        );
        payment.setId(id);
        return payment;
    }
    
    public Payment save(Payment payment) {
        String sql = "INSERT INTO payments (order_id, method, amount_paid, change_amount, is_debt, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        long id = insert(sql,
                payment.getOrderId(),
                payment.getMethod(),
                payment.getAmountPaid(),
                payment.getChangeAmount(),
                payment.isDebt() ? 1 : 0,
                payment.getCreatedAt().toString()
        );
        payment.setId(id);
        return payment;
    }
}
