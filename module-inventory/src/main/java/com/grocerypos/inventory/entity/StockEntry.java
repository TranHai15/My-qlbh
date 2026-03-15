package com.grocerypos.inventory.entity;

import com.grocerypos.core.base.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể Nhập kho (Nhập hàng).
 */
public class StockEntry extends BaseEntity {
    private Long productId;
    private Long supplierId;
    private BigDecimal quantity;
    private BigDecimal costPrice;
    private LocalDateTime entryDate;
    private String note;

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public LocalDateTime getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDateTime entryDate) { this.entryDate = entryDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
