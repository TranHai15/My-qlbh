# 05 — Hướng Dẫn Làm Từng Module

## Nguyên Tắc Chung Khi Nhận Module

1. **Đọc** `docs/01-ARCHITECTURE.md` — hiểu tổng thể
2. **Đọc** `docs/02-DATABASE-SCHEMA.md` — biết các bảng liên quan đến module mình
3. **Đọc** `docs/03-API-CONTRACTS.md` — biết interface cần implement
4. **Đọc** `docs/04-CODING-STANDARDS.md` — tuân theo quy chuẩn
5. **Đọc** `skills/SKILL-<tên-module>.md` — hướng dẫn chi tiết module đó
6. **KHÔNG** sửa interface trong `03-API-CONTRACTS.md` tùy ý

---

## Thứ Tự Xây Dựng Trong Mỗi Module

```
Bước 1: Entity classes  (từ schema DB)
Bước 2: Repository      (CRUD + queries cơ bản)
Bước 3: Service Impl    (business logic)
Bước 4: Unit Tests      (test service với mock repo)
Bước 5: Báo cáo hoàn thành cho team
```

---

## 🔷 Module Core

**Ai làm:** Người có kinh nghiệm nhất — làm đầu tiên, mọi người chờ.

**Deliverables bắt buộc:**
- `DatabaseConfig.java` — HikariCP + SQLite connection pool
- `AppConfig.java` — đọc application.properties
- `BaseRepository.java` — helper methods cho JDBC
- `BaseEntity.java` — base class, tự gán `createdBy` từ SessionManager
- `SessionManager.java` + `UserSession.java` — quản lý phiên đăng nhập
- `AppEventBus.java` — Guava EventBus wrapper
- `AppException`, `ValidationException`, `ResourceNotFoundException`, `InsufficientStockException`
- `MoneyUtils`, `DateTimeUtils`, `PasswordUtils`, `StringUtils`, `BarcodeUtils`
- `schema.sql` — tạo toàn bộ bảng

**Checklist hoàn thành:**
- [ ] `DatabaseConfig.initialize()` tạo DB + chạy schema.sql thành công
- [ ] `AppConfig.get()` đọc đúng từ application.properties
- [ ] `BaseRepository.executeInTransaction()` rollback khi lỗi
- [ ] `SessionManager.login/logout/isLoggedIn` hoạt động đúng
- [ ] `BaseEntity` tự gán `createdBy` từ SessionManager
- [ ] `AppEventBus` publish/subscribe (Guava) hoạt động
- [ ] `PasswordUtils.verify()` đúng với hash BCrypt
- [ ] `MoneyUtils.formatVND(150000)` → `"150.000 ₫"`
- [ ] Tất cả Utils có unit test

---

## 🔷 Module Product

**Phụ thuộc:** `module-core` phải xong trước

**Bảng DB liên quan:** `products`, `categories`, `units`

**Deliverables bắt buộc:**
- Entity: `Product.java`, `Category.java`, `Unit.java`
- Repository: `ProductRepository.java`, `CategoryRepository.java`, `UnitRepository.java`
- Service: `ProductService.java` (interface đã có), `ProductServiceImpl.java`
- Service: `CategoryService.java`, `CategoryServiceImpl.java`

**Logic quan trọng cần implement:**
```
ProductServiceImpl.save():
  - Validate tên, giá bán > 0
  - Nếu barcode null → tự sinh bằng BarcodeUtils
  - Kiểm tra barcode không trùng

ProductServiceImpl.updateStock(productId, delta):
  - delta < 0 = xuất hàng
  - delta > 0 = nhập hàng
  - Nếu tồn kho sau khi trừ < 0 → throw InsufficientStockException
  - Nếu tồn kho sau khi trừ < min_stock → publish LowStockEvent

ProductServiceImpl.findNearExpiry(daysAhead):
  - Trả về sản phẩm có expiry_date trong vòng N ngày tới
```

**Checklist hoàn thành:**
- [ ] CRUD Product hoạt động
- [ ] Tìm theo barcode hoạt động
- [ ] Tìm theo tên (LIKE search) hoạt động
- [ ] updateStock phát hiện hết hàng
- [ ] findNearExpiry hoạt động đúng
- [ ] Unit test coverage > 80%

---

## 🔷 Module Customer

**Phụ thuộc:** `module-core`

**Bảng DB liên quan:** `customers`, `debt_records`

**Deliverables bắt buộc:**
- Entity: `Customer.java`, `DebtRecord.java`
- Repository: `CustomerRepository.java`, `DebtRecordRepository.java`
- Service: `CustomerServiceImpl.java`, `DebtServiceImpl.java`

**Logic quan trọng:**
```
DebtServiceImpl.recordDebt():
  - Insert vào debt_records (type = 'BORROW')
  - Cộng amount vào customers.total_debt
  - Phải là ATOMIC (transaction)

DebtServiceImpl.recordRepayment():
  - Insert vào debt_records (type = 'REPAY')
  - Trừ amount khỏi customers.total_debt
  - Không cho total_debt âm

CustomerServiceImpl.findWithDebt():
  - Chỉ trả về khách có total_debt > 0
  - Sort theo total_debt DESC
```

**Checklist hoàn thành:**
- [ ] CRUD Customer hoạt động
- [ ] Tìm theo số điện thoại hoạt động
- [ ] Ghi nợ / trả nợ cập nhật đúng total_debt
- [ ] Không cho total_debt < 0
- [ ] getHistory trả về đúng thứ tự thời gian

---

## 🔷 Module Inventory

**Phụ thuộc:** `module-core`

**Bảng DB liên quan:** `stock_entries`, `suppliers`

**Deliverables bắt buộc:**
- Entity: `StockEntry.java`, `Supplier.java`
- Repository: `InventoryRepository.java`, `SupplierRepository.java`
- Service: `InventoryServiceImpl.java`

**Logic quan trọng:**
```
InventoryServiceImpl.recordEntry():
  - Insert stock_entries
  - Gọi ProductService.updateStock(productId, +quantity) để tăng tồn kho
  - Lưu ý: InventoryService phụ thuộc ProductService
```

---

## 🔷 Module Promotion

**Phụ thuộc:** `module-core`

**Bảng DB liên quan:** `promotions`, `promotion_rules`

**Logic quan trọng — DiscountEngine:**
```
DiscountEngine.calculate(cart, customer):

  Bước 1: Lấy tất cả promotion đang active (trong thời gian áp dụng)

  Bước 2: Với mỗi promotion, kiểm tra điều kiện:
    - PERCENT: Giảm % trên tổng đơn hàng (nếu đủ min_order_value)
    - FIXED: Giảm tiền cố định (nếu đủ min_order_value)
    - BUY_X_GET_Y: Check từng item trong cart có đủ buy_quantity không
    - COMBO: Check cart có đủ các sản phẩm trong combo không

  Bước 3: Áp dụng discount của khách quen (customer.discount_rate)

  Bước 4: Chọn discount lớn nhất (không stack nhiều KM)
    → Trả về DiscountResult
```

**Checklist hoàn thành:**
- [ ] PERCENT discount đúng
- [ ] FIXED discount đúng
- [ ] BUY_X_GET_Y hoạt động
- [ ] KM có start/end date được lọc đúng
- [ ] Discount khách quen được áp dụng
- [ ] Không stack nhiều KM

---

## 🔷 Module Order

**Phụ thuộc:** `module-core` + `module-product` + `module-customer` + `module-promotion`

**Bảng DB liên quan:** `orders`, `order_items`, `payments`

**Logic quan trọng — OrderService.createOrder():**
```
PHẢI chạy trong 1 DB TRANSACTION:
  1. Sinh order_code = "ORD-" + yyyyMMdd + "-" + sequence
  2. Insert vào orders
  3. Insert từng item vào order_items
     - Snapshot product.name, unit_price, cost_price tại thời điểm bán
  4. Gọi ProductService.updateStock() trừ từng sản phẩm
  5. Insert audit_log
  6. Publish OrderCompletedEvent
  → Nếu bất kỳ bước nào lỗi: ROLLBACK toàn bộ
```

**Logic PaymentService.processPayment():**
```
  1. Insert vào payments
  2. Nếu amount_paid < order.total_amount:
     - is_debt = true
     - Gọi DebtService.recordDebt(customerId, orderId, shortfall)
  3. Tính change_amount = amount_paid - total_amount (nếu > 0)
```

**Checklist hoàn thành:**
- [ ] createOrder chạy atomic (rollback khi lỗi)
- [ ] Trừ stock đúng sau khi tạo order
- [ ] cancelOrder hoàn trả stock
- [ ] Ghi nợ đúng khi khách thiếu tiền
- [ ] order_code không trùng

---

## 🔷 Module Report

**Phụ thuộc:** `module-core` + `module-order` + `module-product` + `module-customer`

**Bảng DB liên quan:** Đọc từ nhiều bảng, KHÔNG ghi

**Lưu ý:** Module này CHỈ ĐỌC, không bao giờ INSERT/UPDATE/DELETE.

**Logic quan trọng:**
```sql
-- Doanh thu & lợi nhuận theo ngày
SELECT
    DATE(o.created_at) as date,
    SUM(o.total_amount) as revenue,
    SUM(oi.cost_price * oi.quantity) as total_cost,
    SUM(o.total_amount) - SUM(oi.cost_price * oi.quantity) as profit,
    COUNT(DISTINCT o.id) as order_count
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
WHERE o.status = 'COMPLETED'
  AND DATE(o.created_at) BETWEEN ? AND ?
GROUP BY DATE(o.created_at)

-- Top sản phẩm bán chạy
SELECT
    p.id, p.name,
    SUM(oi.quantity) as qty_sold,
    SUM(oi.line_total) as revenue
FROM order_items oi
JOIN products p ON oi.product_id = p.id
JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'COMPLETED'
  AND DATE(o.created_at) BETWEEN ? AND ?
GROUP BY p.id
ORDER BY qty_sold DESC
LIMIT ?
```
