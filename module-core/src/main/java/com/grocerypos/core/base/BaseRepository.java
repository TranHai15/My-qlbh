package com.grocerypos.core.base;

import com.grocerypos.core.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Lớp cơ sở cho các Repository, cung cấp các phương thức tiện ích để thao tác với DB.
 * Hỗ trợ Transaction theo yêu cầu của SKILL-core.md.
 */
public abstract class BaseRepository {
    private static final Logger log = LoggerFactory.getLogger(BaseRepository.class);
    private static final ThreadLocal<Connection> threadLocalConn = new ThreadLocal<>();

    protected Connection getConnection() throws SQLException {
        Connection conn = threadLocalConn.get();
        if (conn != null && !conn.isClosed()) {
            return conn;
        }
        return DatabaseConfig.getConnection();
    }

    /**
     * Thực hiện một nhóm công việc trong một Transaction.
     */
    public <T> T executeInTransaction(TransactionTask<T> task) {
        Connection existingConn = threadLocalConn.get();
        if (existingConn != null) {
            try {
                return task.execute(existingConn);
            } catch (Exception e) {
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
            }
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            threadLocalConn.set(conn);
            try {
                T result = task.execute(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                log.error("Rollback transaction do lỗi", e);
                throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Lỗi thực thi transaction", e);
            } finally {
                threadLocalConn.remove();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối database", e);
        }
    }

    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        boolean isTransaction = threadLocalConn.get() != null;
        Connection conn = null;
        try {
            conn = getConnection();
            return queryList(conn, sql, mapper, params);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn dữ liệu", e);
        } finally {
            if (!isTransaction && conn != null) {
                try { conn.close(); } catch (SQLException e) { log.error("Lỗi đóng kết nối", e); }
            }
        }
    }

    protected <T> List<T> queryList(Connection conn, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        }
        return results;
    }

    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        boolean isTransaction = threadLocalConn.get() != null;
        Connection conn = null;
        try {
            conn = getConnection();
            return queryOne(conn, sql, mapper, params);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn dữ liệu", e);
        } finally {
            if (!isTransaction && conn != null) {
                try { conn.close(); } catch (SQLException e) { log.error("Lỗi đóng kết nối", e); }
            }
        }
    }

    protected <T> Optional<T> queryOne(Connection conn, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapper.map(rs));
            }
        }
        return Optional.empty();
    }

    protected long insert(String sql, Object... params) {
        boolean isTransaction = threadLocalConn.get() != null;
        Connection conn = null;
        try {
            conn = getConnection();
            return insert(conn, sql, params);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi ghi dữ liệu", e);
        } finally {
            if (!isTransaction && conn != null) {
                try { conn.close(); } catch (SQLException e) { log.error("Lỗi đóng kết nối", e); }
            }
        }
    }

    protected long insert(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Tạo bản ghi thất bại.");
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) return generatedKeys.getLong(1);
                else throw new SQLException("Không lấy được ID.");
            }
        }
    }

    protected int update(String sql, Object... params) {
        boolean isTransaction = threadLocalConn.get() != null;
        Connection conn = null;
        try {
            conn = getConnection();
            return update(conn, sql, params);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật dữ liệu", e);
        } finally {
            if (!isTransaction && conn != null) {
                try { conn.close(); } catch (SQLException e) { log.error("Lỗi đóng kết nối", e); }
            }
        }
    }

    protected int update(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        }
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionTask<T> {
        T execute(Connection conn) throws Exception;
    }
}
