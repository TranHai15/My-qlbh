package com.grocerypos.product.service.impl;

import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.product.entity.Category;
import com.grocerypos.product.repository.CategoryRepository;
import com.grocerypos.product.service.CategoryService;

import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepo;

    public CategoryServiceImpl(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepo.findAll();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepo.findById(id);
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        return categoryRepo.findByParentId(parentId);
    }

    @Override
    public Category save(Category category) {
        validateCategory(category);
        long id = categoryRepo.save(category);
        category.setId(id);
        return category;
    }

    @Override
    public Category update(Category category) {
        validateCategory(category);
        
        // Ngăn chặn vòng lặp trực tiếp
        if (category.getParentId() != null && category.getParentId().equals(category.getId())) {
            throw new ValidationException("Một danh mục không thể làm cha của chính nó.");
        }

        // Ngăn chặn vòng lặp gián tiếp (A -> B -> A)
        if (category.getParentId() != null) {
            checkCyclicHierarchy(category.getId(), category.getParentId());
        }

        categoryRepo.update(category);
        return category;
    }

    /**
     * Kiểm tra xem việc gán parentId có tạo thành vòng lặp không.
     */
    private void checkCyclicHierarchy(Long currentId, Long newParentId) {
        Long tempParentId = newParentId;
        while (tempParentId != null) {
            if (tempParentId.equals(currentId)) {
                throw new ValidationException("Lỗi: Tạo thành vòng lặp phân cấp (Danh mục cha không thể là danh mục con của chính nó).");
            }
            // Lấy cha của danh mục đang xét
            Optional<Category> parent = categoryRepo.findById(tempParentId);
            tempParentId = parent.map(Category::getParentId).orElse(null);
        }
    }

    @Override
    public void delete(Long id) {
        // 1. Kiểm tra xem có danh mục con không
        List<Category> children = categoryRepo.findByParentId(id);
        if (!children.isEmpty()) {
            throw new ValidationException("Không thể xóa: Danh mục này đang chứa " + children.size() + " danh mục con.");
        }

        // 2. Kiểm tra xem có sản phẩm nào thuộc danh mục này không
        long productCount = categoryRepo.countProductsByCategory(id);
        
        if (productCount > 0) {
            throw new ValidationException("Không thể xóa: Hiện có " + productCount + " sản phẩm đang thuộc danh mục này. Vui lòng chuyển hoặc xóa sản phẩm trước.");
        }

        categoryRepo.delete(id);
    }

    @Override
    public List<Category> getCategoryTree() {
        // Logically this would build a tree structure, for now just return all ordered
        return categoryRepo.findAll();
    }

    private void validateCategory(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new ValidationException("Tên danh mục không được để trống.");
        }
    }
}
