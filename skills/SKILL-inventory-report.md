# SKILL — module-inventory

## Vai Trò Của Bạn
Bạn implement `module-inventory` — quản lý nhập hàng và nhà cung cấp.

## Điều Kiện Tiên Quyết
`module-core` ✅ (module-product nên xong để test liên kết)

## Đọc Bắt Buộc
- `docs/02-DATABASE-SCHEMA.md` — bảng: `stock_entries`, `suppliers`
- `docs/03-API-CONTRACTS.md` — mục InventoryService
- `docs/04-CODING-STANDARDS.md`

---

## Logic Quan Trọng

### recordEntry() — Nhập hàng
```java
public StockEntry recordEntry(StockEntry entry) {
    // 1. Validate
    if (entry.getQuantity() <= 0) throw new ValidationException("Số lượng phải > 0");
    if (entry.getCostPrice() < 0) throw new ValidationException("Giá nhập không hợp lệ");

    // 2. Tính total_cost
    entry.setTotalCost(entry.getQuantity() * entry.getCostPrice());

    // 3. Lưu DB
    StockEntry saved = inventoryRepo.save(entry);

    // 4. Tăng tồn kho (gọi ProductService)
    // LƯU Ý: InventoryService phụ thuộc ProductService
    // Inject ProductService qua constructor
    productService.updateStock(entry.getProductId(), entry.getQuantity());

    return saved;
}
```

---

## Checklist Hoàn Thành
- [ ] `recordEntry()` tăng tồn kho product tương ứng
- [ ] CRUD Supplier hoạt động
- [ ] `getHistory()` filter đúng theo productId và date range
- [ ] Validate quantity > 0 và costPrice >= 0

---
---

# SKILL — module-report

## Vai Trò Của Bạn
Bạn implement `module-report` — báo cáo doanh thu, lợi nhuận, hàng bán chạy, công nợ.

## Điều Kiện Tiên Quyết
`module-order` ✅ (cần có dữ liệu orders để query)

## Đọc Bắt Buộc
- `docs/03-API-CONTRACTS.md` — mục ReportService, tất cả DTOs
- `docs/05-MODULE-GUIDE.md` — mục Module Report (có SQL mẫu)

---

## Nguyên Tắc Bất Di Bất Dịch

> **module-report CHỈ ĐỌC. TUYỆT ĐỐI KHÔNG INSERT / UPDATE / DELETE.**

---

## ReportServiceImpl — Cấu Trúc

```java
// Module này KHÔNG có Repository riêng
// Truy vấn trực tiếp qua DatabaseManager với các JOIN phức tạp

public class ReportServiceImpl implements ReportService {

    private final DatabaseManager dbManager;

    @Override
    public RevenueReport getRevenueReport(LocalDate from, LocalDate to) {
        String sql = """
            SELECT
                SUM(o.total_amount) as revenue,
                SUM(oi.cost_price * oi.quantity) as total_cost,
                COUNT(DISTINCT o.id) as order_count
            FROM orders o
            JOIN order_items oi ON o.id = oi.order_id
            WHERE o.status = 'COMPLETED'
              AND DATE(o.created_at) BETWEEN ? AND ?
            """;
        // ... execute và map
    }
}
```

---

## Các Query Phải Implement

1. **Doanh thu theo ngày** trong khoảng thời gian
2. **Doanh thu theo tháng** trong năm
3. **Top N sản phẩm bán chạy** theo số lượng
4. **Top N sản phẩm bán chậm** (đã có trong kho nhưng ít bán)
5. **Chi tiết giao dịch** (danh sách orders + items)
6. **Tổng hợp công nợ** — tất cả khách còn nợ

---

## Checklist Hoàn Thành
- [ ] Không có câu lệnh INSERT/UPDATE/DELETE
- [ ] `getRevenueReport()` tính đúng profit = revenue - cost
- [ ] `getTopSellingProducts()` sort đúng theo qty_sold DESC
- [ ] `getDebtReport()` chỉ trả khách có total_debt > 0
- [ ] Các query filter đúng `status = 'COMPLETED'` (bỏ qua đơn hủy)
- [ ] Date range filter hoạt động đúng
