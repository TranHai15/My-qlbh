package com.grocerypos.inventory.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.inventory.entity.Supplier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SupplierRepository extends BaseRepository {

    private Supplier mapResultSet(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            s.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }
        
        return s;
    }

    public List<Supplier> findAll() {
        return queryList("SELECT * FROM suppliers WHERE deleted_at IS NULL ORDER BY name ASC", this::mapResultSet);
    }

    public long save(Supplier s) {
        String sql = "INSERT INTO suppliers (name, phone, email, address, created_at) VALUES (?, ?, ?, ?, ?)";
        return insert(sql, s.getName(), s.getPhone(), s.getEmail(), s.getAddress(), s.getCreatedAt());
    }

    public void update(Supplier s) {
        String sql = "UPDATE suppliers SET name = ?, phone = ?, email = ?, address = ? WHERE id = ?";
        update(sql, s.getName(), s.getPhone(), s.getEmail(), s.getAddress(), s.getId());
    }
}
