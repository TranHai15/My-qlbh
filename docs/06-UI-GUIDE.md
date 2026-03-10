# 06 — Hướng Dẫn Làm Giao Diện JavaFX

## 1. Nguyên Tắc Tổng Quát

- Mỗi màn hình = 1 file `.fxml` + 1 file `Controller.java`
- Controller CHỈ xử lý UI event → gọi Service → cập nhật UI
- Controller KHÔNG chứa business logic
- Mọi thao tác DB nặng phải chạy trên **background thread** (JavaFX Task)

---

## 2. Cấu Trúc Controller Chuẩn

```java
public class ProductListController extends BaseController {

    // --- FXML Bindings ---
    @FXML private TableView<Product> productTable;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private ComboBox<Category> categoryFilter;

    // --- Services (inject từ AppContext) ---
    private final ProductService productService;
    private final CategoryService categoryService;

    // --- Constructor injection ---
    public ProductListController() {
        this.productService = AppContext.get(ProductService.class);
        this.categoryService = AppContext.get(CategoryService.class);
    }

    // --- Lifecycle ---
    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadData();
    }

    // --- Event Handlers ---
    @FXML
    private void onAddButtonClick() {
        NavigationHelper.openDialog("product/product-form.fxml", "Thêm Sản Phẩm");
    }

    @FXML
    private void onSearchFieldChanged() {
        String keyword = searchField.getText().trim();
        if (keyword.isBlank()) {
            loadData();
        } else {
            runInBackground(
                () -> productService.search(keyword),
                products -> productTable.setItems(FXCollections.observableList(products)),
                e -> AlertHelper.showError("Lỗi tìm kiếm", e.getMessage())
            );
        }
    }

    // --- Private Helpers ---
    private void setupTable() {
        // Setup columns...
    }

    private void loadData() {
        runInBackground(
            () -> productService.findAll(),
            products -> productTable.setItems(FXCollections.observableList(products)),
            e -> AlertHelper.showError("Lỗi tải dữ liệu", e.getMessage())
        );
    }
}
```

---

## 3. BaseController — Methods Tiện Ích

```java
public abstract class BaseController {

    /**
     * Chạy task nặng trên background thread, cập nhật UI trên FX thread
     */
    protected <T> void runInBackground(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        Task<T> task = new Task<>() {
            @Override protected T call() { return backgroundTask.get(); }
        };

        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));

        new Thread(task).start();
    }

    protected void showLoading(boolean show) { /* hiện/ẩn spinner */ }
}
```

---

## 4. AppContext — Dependency Injection Thủ Công

```java
// AppContext.java — Khởi tạo 1 lần khi app start
public class AppContext {

    private static final Map<Class<?>, Object> registry = new HashMap<>();

    public static void initialize() {
        // DatabaseConfig đã được init trước ở MainApp
        // Repositories nhận connection qua DatabaseConfig.getConnection()
        ProductRepository productRepo = new ProductRepository();
        CategoryRepository categoryRepo = new CategoryRepository();
        CustomerRepository customerRepo = new CustomerRepository();
        OrderRepository orderRepo = new OrderRepository();
        // ... tất cả repos

        // Services
        ProductService productService = new ProductServiceImpl(productRepo, categoryRepo);
        CategoryService categoryService = new CategoryServiceImpl(categoryRepo);
        CustomerService customerService = new CustomerServiceImpl(customerRepo);
        DebtService debtService = new DebtServiceImpl(debtRepo, customerRepo);
        PromotionService promotionService = new PromotionServiceImpl(promotionRepo);
        DiscountEngine discountEngine = new DiscountEngineImpl(promotionService);
        OrderService orderService = new OrderServiceImpl(orderRepo, productService, debtService);
        PaymentService paymentService = new PaymentServiceImpl(paymentRepo, debtService);
        ReportService reportService = new ReportServiceImpl(dbManager);
        InventoryService inventoryService = new InventoryServiceImpl(inventoryRepo, productService);

        // Register
        register(ProductService.class, productService);
        register(CategoryService.class, categoryService);
        register(CustomerService.class, customerService);
        register(DebtService.class, debtService);
        register(PromotionService.class, promotionService);
        register(DiscountEngine.class, discountEngine);
        register(OrderService.class, orderService);
        register(PaymentService.class, paymentService);
        register(ReportService.class, reportService);
        register(InventoryService.class, inventoryService);
    }

    public static <T> void register(Class<T> type, T instance) {
        registry.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        T instance = (T) registry.get(type);
        if (instance == null) throw new IllegalStateException("Service not found: " + type.getName());
        return instance;
    }
}
```

---

## 5. Navigation Helper

```java
public class NavigationHelper {

    private static Stage primaryStage;

    public static void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                NavigationHelper.class.getResource("/fxml/" + fxmlPath));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            log.error("Không thể navigate đến: {}", fxmlPath, e);
        }
    }

    public static void openDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                NavigationHelper.class.getResource("/fxml/" + fxmlPath));
            Stage dialog = new Stage();
            dialog.setTitle(title);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(loader.load()));
            dialog.showAndWait();
        } catch (IOException e) {
            log.error("Không thể mở dialog: {}", fxmlPath, e);
        }
    }
}
```

---

## 6. Alert Helper

```java
public class AlertHelper {

    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showInfo(String title, String message) { /* tương tự */ }
    public static void showWarning(String title, String message) { /* tương tự */ }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        return alert.showAndWait()
            .map(r -> r == ButtonType.OK)
            .orElse(false);
    }
}
```

---

## 7. Màn Hình POS — Luồng Quan Trọng Nhất

```
Layout màn hình POS:
┌────────────────┬──────────────────────────┐
│  Tìm SP / Scan │     Giỏ hàng             │
│                │  ┌─────────────────────┐ │
│  [Barcode field│  │ Tên SP  │ SL │ Giá  │ │
│  [Search field]│  │ ...     │    │      │ │
│                │  └─────────────────────┘ │
│  [Danh sách SP]│                           │
│  (click để thêm│  Tổng: 150,000đ          │
│  vào giỏ)      │  Giảm:  10,000đ          │
│                │  ====                    │
│                │  Thực trả: 140,000đ      │
│                │                           │
│                │  [Khách đưa: ______]     │
│                │  Tiền thối: 10,000đ      │
│                │                           │
│                │  [THANH TOÁN] [HỦY]      │
└────────────────┴──────────────────────────┘

Keyboard shortcuts (BẮT BUỘC implement):
  F1 = Focus barcode field
  F2 = Focus search field
  F3 = Chọn khách hàng
  F4 = Áp dụng khuyến mãi
  F12 = Thanh toán
  Delete = Xóa item đang chọn khỏi giỏ
  +/- = Tăng/giảm số lượng item đang chọn
```

---

## 8. Màu Sắc & Style

```css
/* main.css — Màu chủ đạo */
:root {
    -fx-primary: #2ECC71;        /* xanh lá - màu chính */
    -fx-primary-dark: #27AE60;
    -fx-danger: #E74C3C;          /* đỏ - xóa, hủy */
    -fx-warning: #F39C12;         /* cam - cảnh báo */
    -fx-info: #3498DB;            /* xanh dương - thông tin */
    -fx-background: #F5F5F5;      /* nền xám nhạt */
    -fx-surface: #FFFFFF;         /* nền trắng cho card */
    -fx-text-primary: #2C3E50;    /* chữ chính */
    -fx-text-secondary: #7F8C8D;  /* chữ phụ */
}

/* Button chuẩn */
.btn-primary { -fx-background-color: -fx-primary; -fx-text-fill: white; }
.btn-danger  { -fx-background-color: -fx-danger;  -fx-text-fill: white; }

/* TableView chuẩn */
.table-row-cell:selected { -fx-background-color: derive(-fx-primary, 80%); }
```

---

## 9. Rules Quan Trọng Khi Làm UI

1. **KHÔNG** gọi Service trực tiếp trên FX Application Thread — dùng `runInBackground()`
2. **KHÔNG** cập nhật UI từ background thread — dùng `Platform.runLater()`
3. **KHÔNG** hardcode text — dùng constants hoặc resource bundle
4. **LUÔN** show loading indicator khi load data
5. **LUÔN** confirm trước khi xóa dữ liệu
6. **LUÔN** validate input trước khi gửi lên Service
