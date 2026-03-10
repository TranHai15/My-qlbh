# 🛒 GroceryPOS — Phần Mềm Quản Lý Tạp Hóa

> **Phiên bản:** 1.0.0  
> **Stack:** JavaFX 17 + SQLite + Gradle + MVC  
> **Kiến trúc:** Multi-module, offline-first desktop application

---

## 📁 Cấu Trúc Toàn Bộ Dự Án

```
GroceryPOS/
├── README.md                        ← File này
├── settings.gradle                  ← Khai báo tất cả submodules
├── build.gradle                     ← Root config, dependencies chung
├── gradle.properties                ← Biến môi trường, version numbers
│
├── docs/                            ← Toàn bộ tài liệu dự án
│   ├── 01-ARCHITECTURE.md           ← Kiến trúc tổng thể
│   ├── 02-DATABASE-SCHEMA.md        ← ERD + DDL toàn bộ bảng
│   ├── 03-API-CONTRACTS.md          ← Interface contracts mọi Service
│   ├── 04-CODING-STANDARDS.md       ← Quy chuẩn code bắt buộc
│   ├── 05-MODULE-GUIDE.md           ← Hướng dẫn làm từng module
│   └── 06-UI-GUIDE.md               ← Hướng dẫn làm giao diện JavaFX
│
├── skills/                          ← Bộ skills cho AI
│   ├── SKILL-core.md
│   ├── SKILL-product.md
│   ├── SKILL-customer.md
│   ├── SKILL-order.md
│   ├── SKILL-promotion.md
│   ├── SKILL-inventory.md
│   ├── SKILL-report.md
│   └── SKILL-desktop-ui.md
│
├── module-core/
│   ├── build.gradle
│   └── src/main/java/com/grocerypos/core/
│       ├── base/
│       │   ├── BaseEntity.java              ← id, createdAt, updatedAt, createdBy
│       │   └── BaseRepository.java          ← abstract class, helper JDBC methods
│       ├── config/
│       │   ├── DatabaseConfig.java          ← HikariCP + SQLite init
│       │   └── AppConfig.java               ← đọc application.properties
│       ├── session/
│       │   ├── SessionManager.java          ← Singleton, getCurrentUser()
│       │   └── UserSession.java             ← DTO: userId, username, role, loginTime
│       ├── exception/
│       │   ├── AppException.java
│       │   ├── ValidationException.java
│       │   ├── ResourceNotFoundException.java
│       │   └── InsufficientStockException.java
│       ├── event/
│       │   ├── AppEvent.java                ← marker interface
│       │   └── events/
│       │       ├── OrderCompletedEvent.java
│       │       ├── LowStockEvent.java
│       │       └── DebtRecordedEvent.java
│       └── util/
│           ├── MoneyUtils.java              ← formatVND, làm tròn
│           ├── DateTimeUtils.java           ← format chuẩn VN
│           ├── PasswordUtils.java           ← BCrypt hash + verify
│           ├── StringUtils.java             ← normalize, remove dấu, trim
│           └── BarcodeUtils.java            ← generate + validate barcode
│
├── module-product/
│   ├── build.gradle
│   └── src/main/java/com/grocerypos/product/
│       ├── entity/
│       │   ├── Product.java
│       │   ├── Category.java
│       │   └── Unit.java
│       ├── repository/
│       │   ├── ProductRepository.java
│       │   └── CategoryRepository.java
│       └── service/
│           ├── ProductService.java          ← Interface
│           └── impl/ProductServiceImpl.java
│
├── module-customer/
│   ├── build.gradle
│   └── src/main/java/com/grocerypos/customer/
│       ├── entity/
│       │   ├── Customer.java
│       │   └── DebtRecord.java
│       ├── repository/
│       │   └── CustomerRepository.java
│       └── service/
│           ├── CustomerService.java
│           ├── DebtService.java
│           └── impl/
│
├── module-inventory/
│   ├── build.gradle
│   └── src/main/java/com/grocerypos/inventory/
│       ├── entity/
│       │   ├── StockEntry.java
│       │   └── Supplier.java
│       ├── repository/
│       │   └── InventoryRepository.java
│       └── service/
│           ├── InventoryService.java
│           └── impl/
│
├── module-promotion/
│   ├── build.gradle
│   └── src/main/java/com/grocerypos/promotion/
│       ├── entity/
│       │   ├── Promotion.java
│       │   └── PromotionRule.java
│       ├── repository/
│       │   └── PromotionRepository.java
│       └── service/
│           ├── PromotionService.java
│           ├── DiscountEngine.java
│           └── impl/
│
├── module-order/
│   ├── build.gradle
│   └── src/main/java/com/grocerypos/order/
│       ├── entity/
│       │   ├── Order.java
│       │   ├── OrderItem.java
│       │   └── Payment.java
│       ├── repository/
│       │   └── OrderRepository.java
│       └── service/
│           ├── OrderService.java
│           ├── PaymentService.java
│           └── impl/
│
├── module-report/
│   ├── build.gradle
│   └── src/main/java/com/grocerypos/report/
│       ├── dto/
│       │   ├── RevenueReportDTO.java
│       │   ├── TopProductDTO.java
│       │   └── DebtSummaryDTO.java
│       └── service/
│           ├── ReportService.java
│           └── impl/
│
└── module-desktop-ui/
    ├── build.gradle
    └── src/main/
        ├── java/com/grocerypos/ui/
        │   ├── MainApp.java
        │   ├── AppContext.java              ← DI thủ công, khởi tạo Services
        │   ├── controllers/
        │   │   ├── BaseController.java
        │   │   ├── pos/POSController.java
        │   │   ├── product/ProductController.java
        │   │   ├── customer/CustomerController.java
        │   │   ├── inventory/InventoryController.java
        │   │   ├── promotion/PromotionController.java
        │   │   ├── report/ReportController.java
        │   │   └── settings/SettingsController.java
        │   └── utils/
        │       ├── AlertHelper.java
        │       ├── NavigationHelper.java
        │       └── PrintHelper.java
        └── resources/
            ├── fxml/
            │   ├── main-layout.fxml
            │   ├── pos/pos-view.fxml
            │   ├── product/product-view.fxml
            │   ├── customer/customer-view.fxml
            │   ├── inventory/inventory-view.fxml
            │   ├── promotion/promotion-view.fxml
            │   ├── report/report-view.fxml
            │   └── settings/settings-view.fxml
            ├── css/
            │   ├── main.css
            │   └── pos.css
            ├── images/
            └── db/
                └── schema.sql               ← Script tạo DB khi chạy lần đầu
```

---

## 🚀 Quick Start

```bash
# Clone project
git clone <repo-url>
cd GroceryPOS

# Build tất cả modules
./gradlew build

# Chạy ứng dụng
./gradlew :module-desktop-ui:run
```

---

## 👥 Phân Công Module

| Module | Phụ thuộc | Làm song song được? |
|--------|-----------|-------------------|
| module-core | Không ai | ✅ Làm đầu tiên |
| module-product | core | ✅ Song song nhóm 1 |
| module-customer | core | ✅ Song song nhóm 1 |
| module-inventory | core | ✅ Song song nhóm 1 |
| module-promotion | core | ✅ Song song nhóm 1 |
| module-order | core + product + customer + promotion | ⏳ Sau nhóm 1 |
| module-report | core + order + product + customer | ⏳ Sau order |
| module-desktop-ui | Tất cả | ⏳ Làm sau cùng |

---

## 📖 Đọc Tài Liệu Theo Thứ Tự

1. `docs/01-ARCHITECTURE.md` — Hiểu tổng thể trước
2. `docs/02-DATABASE-SCHEMA.md` — Nắm schema DB
3. `docs/03-API-CONTRACTS.md` — Biết interface cần implement
4. `docs/04-CODING-STANDARDS.md` — Đọc TRƯỚC KHI code
5. `docs/05-MODULE-GUIDE.md` — Hướng dẫn làm module cụ thể
6. `skills/SKILL-<tên-module>.md` — Đọc skill tương ứng module bạn làm
