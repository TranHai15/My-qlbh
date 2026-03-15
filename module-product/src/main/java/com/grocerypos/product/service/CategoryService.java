package com.grocerypos.product.service;

import com.grocerypos.product.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> findAll();
    Optional<Category> findById(Long id);
    List<Category> findByParentId(Long parentId);
    Category save(Category category);
    Category update(Category category);
    void delete(Long id);
    
    /**
     * Lấy cấu trúc cây danh mục.
     */
    List<Category> getCategoryTree();
}
