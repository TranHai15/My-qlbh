# SKILL — module-product

## Vai Trò Của Bạn
Bạn implement `module-product` — quản lý sản phẩm, danh mục, đơn vị tính.

## Điều Kiện Tiên Quyết
`module-core` phải **DONE** trước khi bắt đầu.

## Đọc Bắt Buộc
- `docs/02-DATABASE-SCHEMA.md` — bảng: `products`, `categories`, `units`
- `docs/03-API-CONTRACTS.md` — mục ProductService, CategoryService
- `docs/04-CODING-STANDARDS.md`
- `docs/05-MODULE-GUIDE.md` — mục Module Product

---

## Package Structure

```
module-product/src/main/java/com/grocerypos/product/
├── entity/
│   ├── Product.java
│   ├── Category.java
│   └── Unit.java
├── repository/
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   └── UnitRepository.java
├── service/
│   ├── ProductService.java          ← Interface (copy từ API-CONTRACTS)
│   ├── CategoryService.java         ← Interface
│   └── impl/
│       ├── ProductServiceImpl.java
│       └── CategoryServiceImpl.java
└── exception/
    └── DuplicateBarcodeException.java
```

---

## Entity Spec

### Product.java
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Product extends BaseEntity {
    private String name;           // NOT NULL
    private String barcode;        // UNIQUE, nullable
    private Long categoryId;
    private Long unitId;
    private double costPrice;      // >= 0
    private double sellPrice;      // > 0
    private double stockQuantity;  // >= 0
    private double minStock;       // >= 0, ngưỡng cảnh báo
    private LocalDate expiryDate;  // nullable
    private String imagePath;      // nullable
    private boolean active;

    // Transient — không lưu DB, load kèm khi cần
    @Transient private String categoryName;
    @Transient private String unitName;
}
```

---

## Repository Spec

### ProductRepository — Các query quan trọng

```java
// Tìm theo barcode (dùng nhiều nhất ở POS)
Optional<Product> findByBarcode(String barcode);
// SQL: SELECT * FROM products WHERE barcode = ? AND is_active = 1

// Tìm kiếm (dùng LIKE)
List<Product> search(String keyword);
// SQL: SELECT * FROM products
//      WHERE (name LIKE '%?%' OR barcode LIKE '%?%') AND is_active = 1
//      ORDER BY name LIMIT 50

// Hàng cận hạn
List<Product> findNearExpiry(LocalDate cutoffDate);
// SQL: SELECT * FROM products
//      WHERE expiry_date IS NOT NULL AND expiry_date <= ? AND is_active = 1
//      ORDER BY expiry_date ASC

// Hàng tồn kho thấp
List<Product> findLowStock();
// SQL: SELECT * FROM products
//      WHERE stock_quantity <= min_stock AND is_active = 1
//      ORDER BY stock_quantity ASC

// Update stock (dùng atomic update thay vì đọc-tính-ghi)
void updateStock(Long productId, double delta);
// SQL: UPDATE products SET stock_quantity = stock_quantity + ?,
//                          updated_at = datetime('now','localtime')
//      WHERE id = ?
// Sau đó check: nếu stock_quantity < 0 → throw InsufficientStockException
```

---

## Service Logic Chi Tiết

### ProductServiceImpl.save()
```
1. Validate:
   - name không blank
   - sellPrice > 0
   - costPrice >= 0
   - stockQuantity >= 0

2. Barcode:
   - Nếu barcode null hoặc blank → gọi BarcodeUtils.generate()
   - Kiểm tra barcode chưa tồn tại trong DB → nếu trùng throw DuplicateBarcodeException

3. Lưu DB

4. Publish ProductCreatedEvent(product.id)

5. Return saved product
```

### ProductServiceImpl.updateStock()
```
1. Gọi repo.updateStock(productId, delta)
   (atomic SQL: stock = stock + delta)

2. Đọc lại tồn kho mới

3. Nếu tồn mới < 0 → ROLLBACK, throw InsufficientStockException

4. Nếu tồn mới < min_stock → publish LowStockEvent(productId, newStock)
```

---

## build.gradle

```groovy
dependencies {
    implementation project(':module-core')
    // Không cần thêm gì, dùng JDBC từ core
}
```

---

## Unit Tests Bắt Buộc

File: `ProductServiceImplTest.java`
```
✓ save() với barcode null → tự sinh barcode
✓ save() với barcode trùng → throw DuplicateBarcodeException
✓ save() với sellPrice = 0 → throw ValidationException
✓ findByBarcode() tìm đúng
✓ updateStock() trừ đúng số lượng
✓ updateStock() với delta âm quá lớn → throw InsufficientStockException
✓ findNearExpiry() trả về đúng sản phẩm sắp hết hạn
✓ findLowStock() trả về đúng
```

---

## Checklist Hoàn Thành

- [ ] Tất cả methods trong ProductService interface đã implement
- [ ] Tất cả methods trong CategoryService interface đã implement
- [ ] Không import bất kỳ class nào từ module khác (ngoài module-core)
- [ ] Unit tests pass hết
- [ ] Không có System.out.println — dùng log
- [ ] Tất cả SQL dùng prepared statements
