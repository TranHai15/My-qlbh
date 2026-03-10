# 04 — Coding Standards & Quy Chuẩn Code

> **ĐỌC KỸ TRƯỚC KHI VIẾT BẤT KỲ DÒNG CODE NÀO.**
> Toàn bộ AI và developer PHẢI tuân theo bộ quy chuẩn này.

---

## 1. Naming Conventions

### Classes
```java
// Entity: danh từ, PascalCase
Product, Customer, Order, OrderItem

// Service Interface: danh từ + "Service"
ProductService, CustomerService, OrderService

// Service Impl: Interface + "Impl"
ProductServiceImpl, CustomerServiceImpl

// Repository: Entity + "Repository"
ProductRepository, CustomerRepository

// Controller (JavaFX): Screen + "Controller"
POSController, ProductListController, CustomerFormController

// DTO: Mục đích + "DTO"
RevenueReportDTO, TopProductDTO, DebtSummaryDTO

// Exception: Mô tả + "Exception"
ProductNotFoundException, InsufficientStockException
```

### Methods
```java
// ✅ ĐÚNG — động từ mô tả hành động
findById(), saveProduct(), calculateDiscount()
updateStock(), processPayment(), generateBarcode()

// ❌ SAI
getTheProductById(), doSave(), calc()
```

### Variables
```java
// ✅ ĐÚNG
double totalAmount, int stockQuantity, String customerName

// ❌ SAI
double ta, int sq, String s, String customerNameVariable
```

### Constants
```java
// ✅ ĐÚNG — SCREAMING_SNAKE_CASE
public static final int MAX_ITEMS_PER_ORDER = 100;
public static final String DATE_FORMAT = "dd/MM/yyyy";

// ❌ SAI
public static final int maxItems = 100;
```

---

## 2. Package Structure (BẮT BUỘC)

```
com.grocerypos.<module>/
├── entity/       ← POJO thuần, không chứa logic
├── repository/   ← JDBC/JDBI, chỉ chứa SQL
├── service/      ← Interface
│   └── impl/     ← Implement
└── exception/    ← Exception riêng của module (nếu cần)
```

---

## 3. Entity Standards

```java
// ✅ CHUẨN — Dùng Lombok, extends BaseEntity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {
    private String name;
    private String barcode;
    private double sellPrice;
    private double costPrice;
    private double stockQuantity;
    private boolean isActive;
    // ... các field khác từ schema
}

// BaseEntity — BẮT BUỘC tất cả entity phải extends
@Data
public abstract class BaseEntity {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Quy tắc Entity:**
- Entity CHỈ chứa data, không chứa business logic
- Tên field Java phải match với tên cột DB (camelCase ↔ snake_case)
- Không dùng `null` cho số — dùng `0` hoặc `Optional<>`

---

## 4. Repository Standards

```java
// ✅ CHUẨN
public class ProductRepository extends BaseRepository {

    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ? AND is_active = 1";
        return queryOne(sql, this::mapRow, id);
    }

    public List<Product> findByCategory(Long categoryId) {
        String sql = "SELECT * FROM products WHERE category_id = ? AND is_active = 1 ORDER BY name";
        return queryList(sql, this::mapRow, categoryId);
    }

    public Product save(Product product) {
        String sql = """
            INSERT INTO products (name, barcode, category_id, unit_id,
                cost_price, sell_price, stock_quantity, min_stock,
                expiry_date, image_path, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        long id = insert(sql,
            product.getName(), product.getBarcode(), ...);
        product.setId(id);
        return product;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return Product.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .barcode(rs.getString("barcode"))
            .sellPrice(rs.getDouble("sell_price"))
            // ... map tất cả cột
            .build();
    }
}
```

**Quy tắc Repository:**
- CHỈ chứa SQL, không có business logic
- Dùng `?` parameter binding, TUYỆT ĐỐI không concat string SQL (SQL injection)
- Mọi query trả về `Optional<>` nếu có thể null
- mapRow() phải map ĐẦY ĐỦ tất cả fields

---

## 5. Service Standards

```java
// ✅ CHUẨN
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    // ✅ Constructor injection (không dùng field injection)
    public ProductServiceImpl(ProductRepository productRepo,
                               CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public Product save(Product product) {
        // 1. Validate trước
        validateProduct(product);

        // 2. Business logic
        if (product.getBarcode() == null || product.getBarcode().isBlank()) {
            product.setBarcode(generateBarcode());
        }

        // 3. Persist
        Product saved = productRepo.save(product);

        // 4. Publish event nếu cần
        EventBus.publish(new ProductCreatedEvent(saved.getId()));

        return saved;
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new ValidationException("Tên sản phẩm không được để trống");
        }
        if (product.getSellPrice() <= 0) {
            throw new ValidationException("Giá bán phải lớn hơn 0");
        }
    }
}
```

**Quy tắc Service:**
- PHẢI validate input trước khi xử lý
- Ném `ValidationException` cho lỗi validate
- Ném `NotFoundException` khi không tìm thấy entity
- Không bao giờ trả về `null` — dùng `Optional<>` hoặc empty list

---

## 6. Exception Handling

```java
// ✅ ĐÚNG — Dùng custom exceptions
throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id);
throw new ValidationException("Giá bán không hợp lệ");
throw new InsufficientStockException("Hàng không đủ số lượng: " + productName);

// ❌ SAI — Dùng generic Exception
throw new RuntimeException("error");
throw new Exception("something went wrong");

// ✅ ĐÚNG — Xử lý exception ở Controller, không nuốt exception
try {
    orderService.createOrder(cart, customerId, discount);
} catch (InsufficientStockException e) {
    AlertHelper.showError("Hết hàng", e.getMessage());
} catch (ValidationException e) {
    AlertHelper.showWarning("Lỗi nhập liệu", e.getMessage());
}

// ❌ SAI — Nuốt exception
try {
    orderService.createOrder(cart, customerId, discount);
} catch (Exception e) {
    e.printStackTrace(); // KHÔNG LÀM VẬY
}
```

---

## 7. Logging

```java
// ✅ ĐÚNG
private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

log.info("Tạo sản phẩm mới: {}", product.getName());
log.warn("Tồn kho thấp: {} còn {} {}", product.getName(), qty, unit);
log.error("Lỗi tạo đơn hàng orderId={}", orderId, e);

// ❌ SAI
System.out.println("creating product");
e.printStackTrace();
```

---

## 8. Database Access

```java
// ✅ ĐÚNG — Luôn dùng try-with-resources
try (Connection conn = DatabaseManager.getConnection()) {
    // ...
}

// ❌ SAI — Quên đóng connection
Connection conn = DatabaseManager.getConnection();
// dùng conn... không đóng
```

---

## 9. Null Safety

```java
// ✅ ĐÚNG
Optional<Product> product = productService.findById(id);
product.ifPresentOrElse(
    p -> showProduct(p),
    () -> AlertHelper.showError("Không tìm thấy sản phẩm")
);

// ❌ SAI
Product product = productService.findById(id); // có thể null
product.getName(); // NullPointerException
```

---

## 10. Commit Message Convention

```
feat(product): thêm tính năng tạo mã vạch tự động
fix(order): sửa lỗi tính tiền thối âm
refactor(customer): tách DebtService riêng
test(promotion): thêm test cho BUY_X_GET_Y
docs(schema): cập nhật ERD bảng debt_records
```

Format: `<type>(<module>): <mô tả ngắn bằng tiếng Việt>`

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `style`, `chore`

---

## 11. Code Review Checklist

Trước khi submit code, tự check:
- [ ] Có validate input không?
- [ ] Có xử lý null không?
- [ ] Có đóng DB connection không?
- [ ] Có ném đúng exception không (không dùng RuntimeException chung)?
- [ ] Có log đủ không?
- [ ] Có viết unit test không?
- [ ] Method có quá 30 dòng không? (nếu có → tách nhỏ)
- [ ] Tên biến/method có rõ nghĩa không?
- [ ] Có vi phạm dependency rule không? (module nào import module nào)
