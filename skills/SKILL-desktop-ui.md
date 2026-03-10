# SKILL — module-desktop-ui

## Vai Trò Của Bạn
Bạn implement giao diện JavaFX — tích hợp tất cả modules thành ứng dụng hoàn chỉnh.

## Điều Kiện Tiên Quyết (TẤT CẢ phải DONE)
- `module-core` ✅
- `module-product` ✅
- `module-customer` ✅
- `module-inventory` ✅
- `module-promotion` ✅
- `module-order` ✅
- `module-report` ✅

## Đọc Bắt Buộc
- `docs/03-API-CONTRACTS.md` — biết interface của tất cả Services
- `docs/06-UI-GUIDE.md` — quy chuẩn JavaFX
- `docs/04-CODING-STANDARDS.md`

---

## build.gradle

```groovy
plugins {
    id 'org.openjfx.javafxplugin' version '0.1.0'
}

javafx {
    version = "17"
    modules = ['javafx.controls', 'javafx.fxml', 'javafx.graphics']
}

dependencies {
    implementation project(':module-core')
    implementation project(':module-product')
    implementation project(':module-customer')
    implementation project(':module-inventory')
    implementation project(':module-promotion')
    implementation project(':module-order')
    implementation project(':module-report')

    // Barcode image display
    implementation 'com.google.zxing:javase:3.5.3'

    // Chart cho báo cáo
    // JavaFX built-in charts đã đủ dùng
}

application {
    mainClass = 'com.grocerypos.ui.MainApp'
}
```

---

## Bắt Đầu Từ Đây

### 1. MainApp.java
```java
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Đọc config (application.properties)
        // AppConfig tự load qua static initializer

        // 2. Khởi tạo DB (HikariCP + tự chạy schema.sql)
        DatabaseConfig.initialize();

        // 3. Khởi tạo tất cả services
        AppContext.initialize();

        // 4. Hiện màn hình đăng nhập trước
        NavigationHelper.setPrimaryStage(primaryStage);
        NavigationHelper.navigateTo("auth/login-view.fxml");

        primaryStage.setTitle("GroceryPOS - Quản Lý Tạp Hóa");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

> **Lưu ý:** App luôn mở màn hình **đăng nhập** trước. Sau khi login thành công, `SessionManager.login(session)` được gọi rồi mới navigate vào `main-layout.fxml`. Mọi màn hình cần check `SessionManager.getInstance().isLoggedIn()` trước khi render.

### 2. main-layout.fxml — Layout Khung
```xml
<!-- Sidebar navigation + Content area -->
<BorderPane>
    <left>
        <!-- Navigation sidebar -->
        <VBox styleClass="sidebar">
            <Button text="🛒 Bán Hàng"    onAction="#navPOS"/>
            <Button text="📦 Sản Phẩm"   onAction="#navProducts"/>
            <Button text="👥 Khách Hàng"  onAction="#navCustomers"/>
            <Button text="📥 Nhập Hàng"   onAction="#navInventory"/>
            <Button text="🎁 Khuyến Mãi"  onAction="#navPromotions"/>
            <Button text="📊 Báo Cáo"     onAction="#navReports"/>
            <Button text="⚙️ Cài Đặt"    onAction="#navSettings"/>
        </VBox>
    </left>
    <center>
        <StackPane fx:id="contentArea"/>  <!-- Swap content vào đây -->
    </center>
</BorderPane>
```

---

## Thứ Tự Làm Màn Hình

```
Ưu tiên 1: POS screen       ← Dùng nhiều nhất, làm đầu tiên
Ưu tiên 2: Product CRUD     ← Cần có sản phẩm để test POS
Ưu tiên 3: Customer screen  ← Tìm khách ở POS
Ưu tiên 4: Inventory screen ← Nhập hàng
Ưu tiên 5: Promotion screen
Ưu tiên 6: Report screen
Ưu tiên 7: Settings screen
```

---

## Màn Hình POS — Chi Tiết Nhất

### POSController — State Management
```java
public class POSController extends BaseController {

    // State
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private Customer selectedCustomer = null;
    private DiscountResult currentDiscount = new DiscountResult(0, List.of());

    @FXML private TextField barcodeField;
    @FXML private TableView<CartItem> cartTable;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;
    @FXML private TextField cashInputField;
    @FXML private Label changeLabel;
    @FXML private Label customerLabel;

    @FXML
    public void initialize() {
        setupCartTable();
        setupKeyboardShortcuts();
        setupCashInput();
        barcodeField.requestFocus(); // Focus barcode field ngay khi mở
    }

    // F1 focus barcode, F12 checkout, Delete xóa item...
    private void setupKeyboardShortcuts() {
        Scene scene = barcodeField.getScene();
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F1 -> barcodeField.requestFocus();
                case F12 -> onCheckout();
                case DELETE -> removeSelectedItem();
            }
        });
    }

    @FXML
    private void onBarcodeEnter() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isBlank()) return;

        runInBackground(
            () -> productService.findByBarcode(barcode),
            product -> {
                if (product.isPresent()) {
                    addToCart(product.get(), 1);
                } else {
                    AlertHelper.showWarning("Không tìm thấy", "Mã vạch: " + barcode);
                }
                barcodeField.clear();
                barcodeField.requestFocus();
            },
            e -> AlertHelper.showError("Lỗi", e.getMessage())
        );
    }

    private void addToCart(Product product, double qty) {
        // Nếu sản phẩm đã trong giỏ → tăng số lượng
        Optional<CartItem> existing = cartItems.stream()
            .filter(i -> i.getProduct().getId().equals(product.getId()))
            .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + qty);
        } else {
            cartItems.add(new CartItem(product, qty, product.getSellPrice()));
        }

        recalculate();
    }

    private void recalculate() {
        double subtotal = cartItems.stream()
            .mapToDouble(CartItem::getLineTotal).sum();

        Cart cart = new Cart(new ArrayList<>(cartItems));
        currentDiscount = discountEngine.calculate(cart, selectedCustomer);

        double total = subtotal - currentDiscount.getDiscountAmount();

        subtotalLabel.setText(CurrencyUtils.formatVND(subtotal));
        discountLabel.setText("- " + CurrencyUtils.formatVND(currentDiscount.getDiscountAmount()));
        totalLabel.setText(CurrencyUtils.formatVND(total));

        // Tính tiền thối nếu đã nhập tiền mặt
        updateChange();
    }

    @FXML
    private void onCheckout() {
        if (cartItems.isEmpty()) {
            AlertHelper.showWarning("Giỏ hàng trống", "Vui lòng thêm sản phẩm");
            return;
        }

        double cashAmount = parseCashInput();
        double total = parseTotalLabel();

        if (cashAmount < total) {
            // Bán thiếu — yêu cầu phải có khách hàng
            if (selectedCustomer == null) {
                AlertHelper.showWarning("Thiếu tiền",
                    "Khách trả thiếu. Vui lòng chọn khách hàng để ghi nợ.");
                return;
            }
        }

        runInBackground(
            () -> {
                Cart cart = new Cart(new ArrayList<>(cartItems));
                Long customerId = selectedCustomer != null ? selectedCustomer.getId() : null;
                Order order = orderService.createOrder(cart, customerId, currentDiscount.getDiscountAmount());
                Payment payment = paymentService.processPayment(order.getId(), cashAmount, customerId);
                return Map.of("order", order, "payment", payment);
            },
            result -> {
                Order order = (Order) result.get("order");
                AlertHelper.showInfo("Thành công",
                    "Đơn hàng " + order.getOrderCode() + " đã hoàn thành!");
                PrintHelper.printInvoice(order);
                clearCart();
            },
            e -> AlertHelper.showError("Lỗi thanh toán", e.getMessage())
        );
    }

    private void clearCart() {
        cartItems.clear();
        selectedCustomer = null;
        customerLabel.setText("Khách vãng lai");
        cashInputField.clear();
        changeLabel.setText("0 ₫");
        barcodeField.requestFocus();
    }
}
```

---

## PrintHelper — In Hóa Đơn

```java
public class PrintHelper {

    public static void printInvoice(Order order) {
        // Lấy cài đặt cửa hàng từ Settings
        String shopName = settingsService.get("shop_name");
        String shopAddress = settingsService.get("shop_address");

        // Build nội dung ESC/POS hoặc in qua javax.print
        // Format 80mm paper
        StringBuilder content = new StringBuilder();
        content.append(center(shopName)).append("\n");
        content.append(center(shopAddress)).append("\n");
        content.append(line("-", 48)).append("\n");
        content.append(String.format("%-20s %s\n", "Mã HĐ:", order.getOrderCode()));
        content.append(String.format("%-20s %s\n", "Thời gian:",
            DateUtils.format(order.getCreatedAt())));
        content.append(line("-", 48)).append("\n");

        for (OrderItem item : order.getItems()) {
            content.append(String.format("%-25s\n", item.getProductName()));
            content.append(String.format("  %s x %s = %s\n",
                item.getQuantity(),
                CurrencyUtils.formatVND(item.getUnitPrice()),
                CurrencyUtils.formatVND(item.getLineTotal())));
        }

        content.append(line("=", 48)).append("\n");
        content.append(String.format("%-20s %s\n", "TỔNG:",
            CurrencyUtils.formatVND(order.getTotalAmount())));
        content.append(line("-", 48)).append("\n");
        content.append(center(settingsService.get("invoice_footer"))).append("\n");

        sendToPrinter(content.toString(), settingsService.get("printer_name"));
    }
}
```

---

## Checklist Hoàn Thành

### Chức năng
- [ ] POS: Quét barcode thêm sản phẩm vào giỏ
- [ ] POS: Tìm kiếm sản phẩm theo tên
- [ ] POS: Tăng/giảm số lượng trong giỏ
- [ ] POS: Chọn khách hàng
- [ ] POS: Hiện tự động giảm giá khi đủ điều kiện
- [ ] POS: Thanh toán, in hóa đơn, clear giỏ
- [ ] POS: Ghi nợ tự động khi thiếu tiền
- [ ] Product: CRUD đầy đủ + tìm kiếm + filter category
- [ ] Customer: CRUD + xem lịch sử mua + ghi trả nợ
- [ ] Inventory: Nhập hàng mới + xem lịch sử
- [ ] Promotion: CRUD + xem preview discount
- [ ] Report: Doanh thu biểu đồ + top sản phẩm + công nợ
- [ ] Settings: Thông tin cửa hàng + backup/restore

### UI Quality
- [ ] Keyboard shortcuts POS hoạt động (F1, F12, Delete)
- [ ] Loading spinner khi load data
- [ ] Confirm dialog trước khi xóa
- [ ] Responsive khi resize window
- [ ] Font size đủ lớn, dễ đọc (min 13px)
- [ ] Màu sắc nhất quán theo css/main.css
