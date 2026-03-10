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

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    /**
     * Thực hiện một nhóm công việc trong một Transaction.
     * Tự động commit nếu thành công và rollback nếu có lỗi.
     */
    protected <T> T executeInTransaction(TransactionTask<T> task) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            T result = task.execute(conn);
            
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    log.warn("Đang thực hiện rollback transaction do lỗi: {}", e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Lỗi khi rollback transaction", ex);
                }
            }
            throw new RuntimeException("Lỗi thực thi transaction", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    log.error("Lỗi khi đóng kết nối sau transaction", e);
                }
            }
        }
    }

    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi thực thi queryList: {}", sql, e);
            throw new RuntimeException("Lỗi truy vấn dữ liệu", e);
        }
        return results;
    }

    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi thực thi queryOne: {}", sql, e);
            throw new RuntimeException("Lỗi truy vấn dữ liệu", e);
        }
        return Optional.empty();
    }

    protected long insert(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Việc tạo bản ghi thất bại, không có hàng nào bị ảnh hưởng.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Việc tạo bản ghi thất bại, không lấy được ID.");
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi thực thi insert: {}", sql, e);
            throw new RuntimeException("Lỗi ghi dữ liệu", e);
        }
    }

    protected int update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Lỗi thực thi update: {}", sql, e);
            throw new RuntimeException("Lỗi cập nhật dữ liệu", e);
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
