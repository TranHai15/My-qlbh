package com.grocerypos.product.service;

import com.grocerypos.product.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAll();
    Optional<Product> findById(Long id);
    Optional<Product> findByBarcode(String barcode);
    List<Product> search(String keyword, Long categoryId);
    Product save(Product product);
    Product update(Product product);
    void delete(Long id);
    void updateStock(Long productId, double deltaQuantity);
    void updateCostPrice(Long productId, double newCostPrice);
}
