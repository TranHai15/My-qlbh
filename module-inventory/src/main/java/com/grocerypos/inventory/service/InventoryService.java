package com.grocerypos.inventory.service;

import com.grocerypos.inventory.entity.StockEntry;
import com.grocerypos.inventory.entity.Supplier;
import java.util.List;

public interface InventoryService {
    void addStock(StockEntry entry);
    List<Supplier> getAllSuppliers();
    void addSupplier(Supplier supplier);
    void updateSupplier(Supplier supplier);
    List<StockEntry> getHistoryByProduct(Long productId);
    List<StockEntry> getAllHistory();
}
