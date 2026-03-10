# 03 — API Contracts (Service Interfaces)

> Đây là "hợp đồng" giữa các modules. AI làm UI hoặc module khác chỉ cần biết file này.
> **KHÔNG thay đổi interface mà không thông báo team.**

---

## Package Convention

```
com.grocerypos.<module>.service.<ServiceName>       ← Interface
com.grocerypos.<module>.service.impl.<ServiceName>Impl  ← Implementation
```

---

## 1. ProductService

```java
package com.grocerypos.product.service;

public interface ProductService {
    // --- CRUD ---
    Product save(Product product);
    Product update(Product product);
    void delete(Long id);
    void toggleActive(Long id);

    // --- Query ---
    Optional<Product> findById(Long id);
    Optional<Product> findByBarcode(String barcode);
    List<Product> findAll();
    List<Product> findByCategory(Long categoryId);
    List<Product> search(String keyword);           // tìm theo tên hoặc barcode
    List<Product> findLowStock();                   // tồn kho dưới min_stock
    List<Product> findNearExpiry(int daysAhead);    // sắp hết hạn trong N ngày

    // --- Stock ---
    void updateStock(Long productId, double delta); // delta âm = xuất, dương = nhập
    double getStock(Long productId);

    // --- Barcode ---
    String generateBarcode();                        // tự tạo mã vạch mới
}
```

---

## 2. CategoryService

```java
package com.grocerypos.product.service;

public interface CategoryService {
    Category save(Category category);
    Category update(Category category);
    void delete(Long id);
    List<Category> findAll();
    Optional<Category> findById(Long id);
}
```

---

## 3. CustomerService

```java
package com.grocerypos.customer.service;

public interface CustomerService {
    // --- CRUD ---
    Customer save(Customer customer);
    Customer update(Customer customer);
    void delete(Long id);

    // --- Query ---
    Optional<Customer> findById(Long id);
    Optional<Customer> findByPhone(String phone);
    List<Customer> findAll();
    List<Customer> search(String keyword);          // tên hoặc SĐT
    List<Customer> findWithDebt();                  // khách có nợ > 0

    // --- Discount ---
    double getDiscountRate(Long customerId);        // trả về 0.0 nếu không có
}
```

---

## 4. DebtService

```java
package com.grocerypos.customer.service;

public interface DebtService {
    // Ghi nợ (khi khách mua thiếu)
    DebtRecord recordDebt(Long customerId, Long orderId, double amount, String note);

    // Ghi trả nợ
    DebtRecord recordRepayment(Long customerId, double amount, String note);

    // Lấy lịch sử nợ của 1 khách
    List<DebtRecord> getHistory(Long customerId);

    // Tổng nợ hiện tại
    double getCurrentDebt(Long customerId);

    // Tất cả khách đang có nợ với số tiền tổng
    List<DebtSummary> getAllDebtSummary();
}
```

---

## 5. InventoryService

```java
package com.grocerypos.inventory.service;

public interface InventoryService {
    // Nhập hàng
    StockEntry recordEntry(StockEntry entry);

    // Lịch sử nhập hàng
    List<StockEntry> getHistory(Long productId);
    List<StockEntry> getHistoryByDateRange(LocalDate from, LocalDate to);

    // Nhà cung cấp
    Supplier saveSupplier(Supplier supplier);
    List<Supplier> getAllSuppliers();
}
```

---

## 6. PromotionService

```java
package com.grocerypos.promotion.service;

public interface PromotionService {
    // --- CRUD ---
    Promotion save(Promotion promotion);
    Promotion update(Promotion promotion);
    void delete(Long id);
    void toggleActive(Long id);

    // --- Query ---
    List<Promotion> findAll();
    List<Promotion> findActive();           // đang trong thời gian áp dụng
    Optional<Promotion> findById(Long id);
}
```

---

## 7. DiscountEngine

```java
package com.grocerypos.promotion.service;

public interface DiscountEngine {
    /**
     * Tính toán và áp dụng khuyến mãi cho giỏ hàng.
     * @param cart      Giỏ hàng hiện tại
     * @param customer  null nếu khách vãng lai
     * @return          DiscountResult chứa số tiền giảm và danh sách KM áp dụng
     */
    DiscountResult calculate(Cart cart, Customer customer);
}
```

---

## 8. OrderService

```java
package com.grocerypos.order.service;

public interface OrderService {
    /**
     * Tạo đơn hàng hoàn chỉnh.
     * Tự động: tạo order, order_items, trừ stock, ghi log
     */
    Order createOrder(Cart cart, Long customerId, double discountAmount);

    // Hủy đơn hàng (hoàn tồn kho)
    void cancelOrder(Long orderId);

    // Query
    Optional<Order> findById(Long id);
    Optional<Order> findByCode(String code);
    List<Order> findByDateRange(LocalDateTime from, LocalDateTime to);
    List<Order> findByCustomer(Long customerId);
    List<OrderItem> getOrderItems(Long orderId);
}
```

---

## 9. PaymentService

```java
package com.grocerypos.order.service;

public interface PaymentService {
    /**
     * Xử lý thanh toán.
     * Tự động ghi nợ nếu amountPaid < order.totalAmount
     */
    Payment processPayment(Long orderId, double amountPaid, Long customerId);

    // Tính tiền thối
    double calculateChange(double totalAmount, double amountPaid);

    // Lịch sử thanh toán
    Payment findByOrderId(Long orderId);
}
```

---

## 10. ReportService

```java
package com.grocerypos.report.service;

public interface ReportService {
    // Doanh thu & lợi nhuận
    RevenueReport getRevenueReport(LocalDate from, LocalDate to);
    RevenueReport getDailyRevenue(LocalDate date);
    List<RevenueReport> getMonthlyRevenue(int year);

    // Sản phẩm
    List<TopProductDTO> getTopSellingProducts(LocalDate from, LocalDate to, int limit);
    List<TopProductDTO> getSlowMovingProducts(LocalDate from, LocalDate to, int limit);

    // Giao dịch
    List<Order> getTransactionDetail(LocalDate from, LocalDate to);

    // Công nợ
    List<DebtSummaryDTO> getDebtReport();
}
```

---

## DTOs & Value Objects Dùng Chung

```java
// Cart — giỏ hàng trong memory (không lưu DB)
public class Cart {
    private List<CartItem> items;
    private double subtotal;
}

public class CartItem {
    private Product product;
    private double quantity;
    private double unitPrice;
    private double lineTotal;
}

// DiscountResult
public class DiscountResult {
    private double discountAmount;
    private List<String> appliedPromotions;  // tên KM đã áp dụng
}

// RevenueReport
public class RevenueReport {
    private LocalDate date;
    private double revenue;
    private double cost;
    private double profit;
    private int orderCount;
}

// TopProductDTO
public class TopProductDTO {
    private Long productId;
    private String productName;
    private double quantitySold;
    private double revenue;
}

// DebtSummaryDTO
public class DebtSummaryDTO {
    private Long customerId;
    private String customerName;
    private String phone;
    private double totalDebt;
}
```
