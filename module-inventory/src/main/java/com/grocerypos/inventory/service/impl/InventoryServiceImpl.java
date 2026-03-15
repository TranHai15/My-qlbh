package com.grocerypos.inventory.service.impl;

import com.grocerypos.inventory.entity.StockEntry;
import com.grocerypos.inventory.entity.Supplier;
import com.grocerypos.inventory.repository.StockEntryRepository;
import com.grocerypos.inventory.repository.SupplierRepository;
import com.grocerypos.inventory.service.InventoryService;
import com.grocerypos.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryServiceImpl implements InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final StockEntryRepository stockEntryRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;

    public InventoryServiceImpl(StockEntryRepository stockEntryRepo, SupplierRepository supplierRepo, ProductRepository productRepo) {
        this.stockEntryRepo = stockEntryRepo;
        this.supplierRepo = supplierRepo;
        this.productRepo = productRepo;
    }

    @Override
    public void addStock(StockEntry entry) {
        if (entry.getEntryDate() == null) entry.setEntryDate(LocalDateTime.now());
        
        stockEntryRepo.executeInTransaction(conn -> {
            stockEntryRepo.save(entry);
            productRepo.updateStock(entry.getProductId(), entry.getQuantity().doubleValue());
            productRepo.updateCostPrice(entry.getProductId(), entry.getCostPrice().doubleValue());
            return null;
        });
        
        log.info("Đã nhập thêm {} sản phẩm (ID: {}) vào kho.", entry.getQuantity(), entry.getProductId());
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepo.findAll();
    }

    @Override
    public void addSupplier(Supplier supplier) {
        if (supplier.getCreatedAt() == null) supplier.setCreatedAt(LocalDateTime.now());
        supplierRepo.save(supplier);
        log.info("Đã thêm nhà cung cấp mới: {}", supplier.getName());
    }

    @Override
    public void updateSupplier(Supplier supplier) {
        supplierRepo.update(supplier);
        log.info("Đã cập nhật thông tin nhà cung cấp: {}", supplier.getName());
    }

    @Override
    public List<StockEntry> getHistoryByProduct(Long productId) {
        return stockEntryRepo.findByProductId(productId);
    }

    @Override
    public List<StockEntry> getAllHistory() {
        return stockEntryRepo.findAll();
    }
}
