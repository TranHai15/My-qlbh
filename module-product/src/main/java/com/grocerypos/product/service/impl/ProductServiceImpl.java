package com.grocerypos.product.service.impl;

import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.product.entity.Product;
import com.grocerypos.product.repository.ProductRepository;
import com.grocerypos.product.service.ProductService;

import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepo;

    public ProductServiceImpl(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    public List<Product> findAll() {
        return productRepo.findAll();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepo.findById(id);
    }

    @Override
    public Optional<Product> findByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) return Optional.empty();
        return productRepo.findByBarcode(barcode);
    }

    @Override
    public List<Product> search(String keyword, Long categoryId) {
        return productRepo.search(keyword, categoryId);
    }

    @Override
    public Product save(Product product) {
        validateProduct(product);
        // Kiểm tra mã vạch trùng lặp
        if (product.getBarcode() != null && !product.getBarcode().isBlank()) {
            productRepo.findByBarcode(product.getBarcode()).ifPresent(p -> {
                throw new ValidationException("Mã vạch '" + product.getBarcode() + "' đã tồn tại cho sản phẩm: " + p.getName());
            });
        }
        long id = productRepo.save(product);
        product.setId(id);
        return product;
    }

    @Override
    public Product update(Product product) {
        validateProduct(product);
        // Kiểm tra mã vạch trùng lặp (trừ chính nó)
        if (product.getBarcode() != null && !product.getBarcode().isBlank()) {
            productRepo.findByBarcode(product.getBarcode()).ifPresent(p -> {
                if (!p.getId().equals(product.getId())) {
                    throw new ValidationException("Mã vạch '" + product.getBarcode() + "' đã được sử dụng bởi sản phẩm khác.");
                }
            });
        }
        productRepo.update(product);
        return product;
    }

    @Override
    public void delete(Long id) {
        // Tương lai có thể kiểm tra xem sản phẩm có trong đơn hàng nào không
        productRepo.delete(id);
    }

    @Override
    public void updateStock(Long productId, double deltaQuantity) {
        Product product = findById(productId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy sản phẩm với ID: " + productId));
        product.setStockQuantity(product.getStockQuantity() + deltaQuantity);
        productRepo.update(product);
    }

    @Override
    public void updateCostPrice(Long productId, double newCostPrice) {
        if (newCostPrice < 0) throw new ValidationException("Giá vốn không được nhỏ hơn 0.");
        productRepo.updateCostPrice(productId, newCostPrice);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new ValidationException("Tên sản phẩm không được để trống.");
        }
        if (product.getSellPrice() < 0) {
            throw new ValidationException("Giá bán không được nhỏ hơn 0.");
        }
        if (product.getCostPrice() < 0) {
            throw new ValidationException("Giá vốn không được nhỏ hơn 0.");
        }
    }
}
