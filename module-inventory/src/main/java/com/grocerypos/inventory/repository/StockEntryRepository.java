package com.grocerypos.inventory.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.inventory.entity.StockEntry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class StockEntryRepository extends BaseRepository {

    public List<StockEntry> findAll() {
        return queryList("SELECT * FROM stock_entries ORDER BY entry_date DESC", this::mapResultSet);
    }

    private StockEntry mapResultSet(ResultSet rs) throws SQLException {
        StockEntry entry = new StockEntry();
        entry.setId(rs.getLong("id"));
        entry.setProductId(rs.getLong("product_id"));
        entry.setSupplierId(rs.getLong("supplier_id"));
        entry.setQuantity(rs.getBigDecimal("quantity"));
        entry.setCostPrice(rs.getBigDecimal("cost_price"));
        
        String entryDate = rs.getString("entry_date");
        if (entryDate != null) {
            entry.setEntryDate(LocalDateTime.parse(entryDate.replace(" ", "T")));
        }
        
        entry.setNote(rs.getString("note"));
        return entry;
    }

    public long save(StockEntry entry) {
        String sql = "INSERT INTO stock_entries (product_id, supplier_id, quantity, cost_price, entry_date, note) VALUES (?, ?, ?, ?, ?, ?)";
        return insert(sql, entry.getProductId(), entry.getSupplierId(), entry.getQuantity(), entry.getCostPrice(), entry.getEntryDate(), entry.getNote());
    }

    public List<StockEntry> findByProductId(Long productId) {
        return queryList("SELECT * FROM stock_entries WHERE product_id = ?", this::mapResultSet, productId);
    }
}
