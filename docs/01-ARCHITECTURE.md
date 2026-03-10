# 01 — Kiến Trúc Tổng Thể GroceryPOS

## 1. Tổng Quan

GroceryPOS là ứng dụng desktop **offline-first** viết bằng Java, chạy trên Windows/macOS/Linux. Không cần internet để hoạt động. Dữ liệu lưu hoàn toàn trên máy local qua SQLite.

---

## 2. Sơ Đồ Kiến Trúc

```
┌─────────────────────────────────────────────────────────┐
│                   module-desktop-ui                       │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐ │
│  │  JavaFX View│  │  Controllers │  │   AppContext     │ │
│  │  (.fxml)    │←→│  (UI Logic)  │←→│  (DI Container) │ │
│  └─────────────┘  └──────┬───────┘  └─────────────────┘ │
└─────────────────────────┼───────────────────────────────┘
                           │ gọi Service interfaces
         ┌─────────────────┼──────────────────────┐
         ↓                 ↓                       ↓
┌──────────────┐  ┌───────────────┐  ┌────────────────────┐
│module-product│  │module-customer│  │   module-order     │
│module-inventory  │module-promotion  │   module-report    │
└──────┬───────┘  └───────┬───────┘  └────────┬───────────┘
       └──────────────────┼────────────────────┘
                          ↓
               ┌──────────────────┐
               │   module-core    │
               │ DB / Utils /     │
               │ Exceptions /     │
               │ EventBus         │
               └────────┬─────────┘
                        ↓
               ┌──────────────────┐
               │   SQLite DB      │
               │  grocerypos.db   │
               └──────────────────┘
```

---

## 3. Nguyên Tắc Kiến Trúc

### 3.1 Dependency Rule (BẮT BUỘC)
```
Chiều phụ thuộc CHỈ được đi theo một chiều:
UI → Service Modules → Core → Database

TUYỆT ĐỐI KHÔNG được:
- module-product import từ module-customer
- module-core import từ bất kỳ module nào khác
- Bất kỳ module nào import từ module-desktop-ui
```

### 3.2 Layer trong mỗi Module
```
Controller (UI) → Service (Business Logic) → Repository (Data Access) → DB
```

Mỗi layer CHỈ giao tiếp với layer liền kề. Controller không được gọi Repository trực tiếp.

### 3.3 Interface-First
Mọi Service PHẢI có Interface trước khi implement:
```java
// ✅ ĐÚNG
public interface ProductService { ... }
public class ProductServiceImpl implements ProductService { ... }

// ❌ SAI - không có interface
public class ProductService { ... }
```

### 3.4 Giao Tiếp Giữa Modules
Modules KHÔNG import lẫn nhau (trừ core). Khi cần thông báo sự kiện, dùng **EventBus**:
```java
// module-order phát sự kiện sau khi bán hàng
EventBus.publish(new OrderCompletedEvent(orderId, items));

// module-inventory lắng nghe để trừ tồn kho
EventBus.subscribe(OrderCompletedEvent.class, this::onOrderCompleted);
```

---

## 4. Stack Công Nghệ

| Thành phần | Công nghệ | Lý do chọn |
|-----------|-----------|------------|
| Language | Java 17 LTS | Ổn định, long-term support |
| UI Framework | JavaFX 17 | Modern, FXML, CSS styling |
| Database | SQLite 3 | Offline, zero-config, file-based |
| DB Access | JDBI3 | Nhẹ hơn Hibernate, phù hợp SQLite |
| Build | Gradle 8 | Multi-module support tốt hơn Maven |
| Barcode | ZXing | Tạo + đọc mã vạch |
| Printing | javax.print + ESC/POS | In hóa đơn nhiệt |
| Logging | SLF4J + Logback | Standard logging |
| Testing | JUnit 5 + Mockito | Unit test |
| Lombok | Lombok 1.18 | Giảm boilerplate |

---

## 5. Vòng Đời Dữ Liệu — Ví Dụ Bán Hàng

```
Nhân viên quét mã vạch
        ↓
POSController.onBarcodeScanned(barcode)
        ↓
ProductService.findByBarcode(barcode)      ← module-product
        ↓
Thêm vào giỏ hàng (Cart trong memory)
        ↓
POSController.onCheckout()
        ↓
PromotionService.applyPromotions(cart)     ← module-promotion
        ↓
OrderService.createOrder(cart, customerId) ← module-order
        ↓
PaymentService.processPayment(order, cash) ← module-order
        ↓
EventBus.publish(OrderCompletedEvent)
        ↓
InventoryService.deductStock(items)        ← module-inventory (lắng nghe event)
        ↓
PrintHelper.printInvoice(order)            ← module-desktop-ui
```

---

## 6. Cấu Trúc Database File

Database file `grocerypos.db` được tạo tự động lần đầu chạy app, lưu tại:
- Windows: `%APPDATA%/GroceryPOS/grocerypos.db`
- macOS: `~/Library/Application Support/GroceryPOS/grocerypos.db`
- Linux: `~/.config/GroceryPOS/grocerypos.db`

Script tạo schema: `module-desktop-ui/src/main/resources/db/schema.sql`

---

## 7. Quy Trình Backup

- Backup = copy file `grocerypos.db` ra ngoài
- Restore = thay file DB mới vào
- Admin có thể trigger qua Settings UI
- Tự động backup mỗi ngày vào `backup/` folder
