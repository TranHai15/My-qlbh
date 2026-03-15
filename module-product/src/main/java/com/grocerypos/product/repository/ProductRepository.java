package com.grocerypos.product.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.product.entity.Product;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductRepository extends BaseRepository {

    public List<Product> findAll() {
        String sql = "SELECT p.*, c.name as category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.name ASC";
        return queryList(sql, this::mapRow);
    }

    public Optional<Product> findById(Long id) {
        String sql = "SELECT p.*, c.name as category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        return queryOne(sql, this::mapRow, id);
    }

    public Optional<Product> findByBarcode(String barcode) {
        String sql = "SELECT p.*, c.name as category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.barcode = ?";
        return queryOne(sql, this::mapRow, barcode);
    }

    public List<Product> search(String keyword, Long categoryId) {
        StringBuilder sql = new StringBuilder("SELECT p.*, c.name as category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE 1=1");
        Object[] params;
        
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.name LIKE ? OR p.barcode LIKE ?)");
        }
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
        }
        sql.append(" ORDER BY p.name ASC");

        if (keyword != null && !keyword.isBlank() && categoryId != null) {
            params = new Object[]{"%" + keyword + "%", "%" + keyword + "%", categoryId};
        } else if (keyword != null && !keyword.isBlank()) {
            params = new Object[]{"%" + keyword + "%", "%" + keyword + "%"};
        } else if (categoryId != null) {
            params = new Object[]{categoryId};
        } else {
            params = new Object[]{};
        }

        return queryList(sql.toString(), this::mapRow, params);
    }

    public long save(Product product) {
        String sql = "INSERT INTO products (name, barcode, category_id, cost_price, sell_price, stock_quantity, is_active, image_url, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return insert(sql, product.getName(), product.getBarcode(), product.getCategoryId(), 
                     product.getCostPrice(), product.getSellPrice(), product.getStockQuantity(), product.isActive() ? 1 : 0,
                     product.getImageUrl(), product.getDescription());
    }

    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, barcode = ?, category_id = ?, cost_price = ?, sell_price = ?, stock_quantity = ?, is_active = ?, image_url = ?, description = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        update(sql, product.getName(), product.getBarcode(), product.getCategoryId(), 
               product.getCostPrice(), product.getSellPrice(), product.getStockQuantity(), product.isActive() ? 1 : 0, 
               product.getImageUrl(), product.getDescription(), product.getId());
    }

    public void updateStock(Long productId, double delta) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        update(sql, delta, productId);
    }

    public void updateCostPrice(Long productId, double costPrice) {
        String sql = "UPDATE products SET cost_price = ? WHERE id = ?";
        update(sql, costPrice, productId);
    }

    public void delete(Long id) {
        update("DELETE FROM products WHERE id = ?", id);
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = Product.builder()
                .name(rs.getString("name"))
                .barcode(rs.getString("barcode"))
                .categoryId(rs.getObject("category_id") != null ? rs.getLong("category_id") : null)
                .costPrice(rs.getDouble("cost_price"))
                .sellPrice(rs.getDouble("sell_price"))
                .stockQuantity(rs.getDouble("stock_quantity"))
                .imageUrl(rs.getString("image_url"))
                .description(rs.getString("description"))
                .isActive(rs.getInt("is_active") == 1)
                .categoryName(rs.getString("category_name"))
                .build();
        p.setId(rs.getLong("id"));
        return p;
    }
}
