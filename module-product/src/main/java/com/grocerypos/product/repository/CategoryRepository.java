package com.grocerypos.product.repository;

import com.grocerypos.core.base.BaseRepository;
import com.grocerypos.product.entity.Category;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CategoryRepository extends BaseRepository {

    public List<Category> findAll() {
        String sql = "SELECT c.*, p.name as parent_name FROM categories c LEFT JOIN categories p ON c.parent_id = p.id ORDER BY c.id ASC";
        return queryList(sql, this::mapRow);
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT c.*, p.name as parent_name FROM categories c LEFT JOIN categories p ON c.parent_id = p.id WHERE c.id = ?";
        return queryOne(sql, this::mapRow, id);
    }

    public List<Category> findByParentId(Long parentId) {
        String sql = "SELECT c.*, p.name as parent_name FROM categories c LEFT JOIN categories p ON c.parent_id = p.id WHERE c.parent_id = ?";
        return queryList(sql, this::mapRow, parentId);
    }

    public long save(Category category) {
        String sql = "INSERT INTO categories (name, description, parent_id, is_active, image_url) VALUES (?, ?, ?, ?, ?)";
        return insert(sql, category.getName(), category.getDescription(), category.getParentId(), 
                      category.isActive() ? 1 : 0, category.getImageUrl());
    }

    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, parent_id = ?, is_active = ?, image_url = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        update(sql, category.getName(), category.getDescription(), category.getParentId(), 
               category.isActive() ? 1 : 0, category.getImageUrl(), category.getId());
    }

    public void delete(Long id) {
        update("DELETE FROM categories WHERE id = ?", id);
    }

    public long countProductsByCategory(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        return queryOne(sql, rs -> rs.getLong(1), categoryId).orElse(0L);
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = Category.builder()
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .parentId(rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null)
                .isActive(rs.getInt("is_active") == 1)
                .parentName(rs.getString("parent_name"))
                .imageUrl(rs.getString("image_url"))
                .build();
        c.setId(rs.getLong("id"));
        return c;
    }
}
