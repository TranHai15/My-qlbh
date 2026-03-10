# 02 — Database Schema (ERD + DDL)

## Sơ Đồ Quan Hệ (ERD)

```
categories ──< products >── units
                  │
                  ├──< order_items >── orders >── customers
                  │                       │
                  └──< stock_entries       └──< payments
                                          │
                                     promotions
                                          │
                                   promotion_rules

customers ──< debt_records
suppliers ──< stock_entries
```

---

## DDL — Toàn Bộ Script Tạo Bảng

```sql
-- ============================================================
-- GroceryPOS Database Schema v1.0
-- Engine: SQLite 3
-- ============================================================

PRAGMA journal_mode=WAL;
PRAGMA foreign_keys=ON;

-- ------------------------------------------------------------
-- CATEGORIES — Danh mục sản phẩm
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    description TEXT,
    is_active   INTEGER NOT NULL DEFAULT 1,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- UNITS — Đơn vị tính
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS units (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,           -- vd: "Thùng", "Chai", "Kg"
    base_unit_id    INTEGER REFERENCES units(id),-- null = đây là đơn vị gốc
    conversion_rate REAL    NOT NULL DEFAULT 1.0,-- 1 thùng = 24 chai → rate=24
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- PRODUCTS — Sản phẩm
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,
    barcode         TEXT    UNIQUE,
    category_id     INTEGER REFERENCES categories(id),
    unit_id         INTEGER REFERENCES units(id),
    cost_price      REAL    NOT NULL DEFAULT 0,     -- Giá vốn
    sell_price      REAL    NOT NULL,               -- Giá bán
    stock_quantity  REAL    NOT NULL DEFAULT 0,     -- Tồn kho
    min_stock       REAL    NOT NULL DEFAULT 0,     -- Ngưỡng cảnh báo hết hàng
    expiry_date     TEXT,                           -- Hạn sử dụng (có thể null)
    image_path      TEXT,                           -- Đường dẫn ảnh
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);

-- ------------------------------------------------------------
-- CUSTOMERS — Khách hàng
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,
    phone           TEXT    UNIQUE,
    address         TEXT,
    discount_rate   REAL    NOT NULL DEFAULT 0,     -- % chiết khấu riêng
    total_debt      REAL    NOT NULL DEFAULT 0,     -- Tổng công nợ hiện tại
    notes           TEXT,
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);

-- ------------------------------------------------------------
-- DEBT_RECORDS — Lịch sử công nợ
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS debt_records (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL REFERENCES customers(id),
    order_id    INTEGER REFERENCES orders(id),       -- null nếu là trả nợ thủ công
    type        TEXT    NOT NULL,                    -- 'BORROW' | 'REPAY'
    amount      REAL    NOT NULL,
    note        TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- SUPPLIERS — Nhà cung cấp
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    phone       TEXT,
    address     TEXT,
    notes       TEXT,
    is_active   INTEGER NOT NULL DEFAULT 1,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- STOCK_ENTRIES — Nhập hàng
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_entries (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id      INTEGER NOT NULL REFERENCES products(id),
    supplier_id     INTEGER REFERENCES suppliers(id),
    quantity        REAL    NOT NULL,
    cost_price      REAL    NOT NULL,           -- Giá nhập tại thời điểm đó
    total_cost      REAL    NOT NULL,
    note            TEXT,
    entry_date      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- PROMOTIONS — Chương trình khuyến mãi
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS promotions (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,
    type            TEXT    NOT NULL,   -- 'PERCENT' | 'FIXED' | 'BUY_X_GET_Y' | 'COMBO'
    value           REAL    NOT NULL DEFAULT 0,   -- % hoặc số tiền
    min_order_value REAL    NOT NULL DEFAULT 0,   -- Đơn tối thiểu để áp dụng
    start_date      TEXT,
    end_date        TEXT,
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- PROMOTION_RULES — Chi tiết điều kiện khuyến mãi
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS promotion_rules (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    promotion_id    INTEGER NOT NULL REFERENCES promotions(id),
    product_id      INTEGER REFERENCES products(id),     -- null = áp dụng toàn đơn
    category_id     INTEGER REFERENCES categories(id),   -- null = không giới hạn category
    buy_quantity    REAL    DEFAULT 0,   -- Cho BUY_X_GET_Y: mua X
    get_quantity    REAL    DEFAULT 0    -- Cho BUY_X_GET_Y: được Y
);

-- ------------------------------------------------------------
-- ORDERS — Đơn hàng
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    order_code      TEXT    NOT NULL UNIQUE,        -- vd: ORD-20240115-001
    customer_id     INTEGER REFERENCES customers(id),-- null = khách vãng lai
    subtotal        REAL    NOT NULL,               -- Tổng trước giảm giá
    discount_amount REAL    NOT NULL DEFAULT 0,
    total_amount    REAL    NOT NULL,               -- Tổng sau giảm giá
    status          TEXT    NOT NULL DEFAULT 'COMPLETED', -- 'COMPLETED' | 'CANCELLED'
    notes           TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_created  ON orders(created_at);

-- ------------------------------------------------------------
-- ORDER_ITEMS — Chi tiết đơn hàng
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id        INTEGER NOT NULL REFERENCES orders(id),
    product_id      INTEGER NOT NULL REFERENCES products(id),
    product_name    TEXT    NOT NULL,   -- snapshot tên lúc bán
    unit_price      REAL    NOT NULL,   -- snapshot giá lúc bán
    cost_price      REAL    NOT NULL,   -- snapshot giá vốn để tính lợi nhuận
    quantity        REAL    NOT NULL,
    discount_amount REAL    NOT NULL DEFAULT 0,
    line_total      REAL    NOT NULL
);

-- ------------------------------------------------------------
-- PAYMENTS — Thanh toán
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id        INTEGER NOT NULL REFERENCES orders(id),
    method          TEXT    NOT NULL DEFAULT 'CASH',    -- 'CASH' | 'DEBT'
    amount_paid     REAL    NOT NULL,
    change_amount   REAL    NOT NULL DEFAULT 0,
    is_debt         INTEGER NOT NULL DEFAULT 0,         -- 1 = khách nợ
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- SETTINGS — Cấu hình hệ thống
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS settings (
    key         TEXT    PRIMARY KEY,
    value       TEXT    NOT NULL,
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- Insert default settings
INSERT OR IGNORE INTO settings (key, value) VALUES
    ('shop_name',       'Tạp Hóa Của Tôi'),
    ('shop_address',    ''),
    ('shop_phone',      ''),
    ('logo_path',       ''),
    ('printer_name',    ''),
    ('paper_width',     '80'),
    ('invoice_footer',  'Cảm ơn quý khách!'),
    ('admin_password',  '$2a$10$defaultHashedPassword'),
    ('backup_path',     '');

-- ------------------------------------------------------------
-- AUDIT_LOG — Lịch sử thao tác
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    action      TEXT    NOT NULL,   -- 'CREATE_ORDER', 'DELETE_PRODUCT', ...
    target_type TEXT    NOT NULL,   -- 'ORDER', 'PRODUCT', ...
    target_id   INTEGER,
    detail      TEXT,               -- JSON string chi tiết
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);
```

---

## Mô Tả Các Bảng Quan Trọng

### orders.status
| Giá trị | Ý nghĩa |
|---------|---------|
| `COMPLETED` | Đơn hàng đã hoàn thành |
| `CANCELLED` | Đơn đã hủy (không trừ tồn kho) |

### payments.method
| Giá trị | Ý nghĩa |
|---------|---------|
| `CASH` | Tiền mặt đủ |
| `DEBT` | Khách mua thiếu (ghi nợ) |

### promotions.type
| Giá trị | Ý nghĩa | value |
|---------|---------|-------|
| `PERCENT` | Giảm % | 10 = giảm 10% |
| `FIXED` | Giảm tiền cố định | 5000 = giảm 5.000đ |
| `BUY_X_GET_Y` | Mua X tặng Y | Xem promotion_rules |
| `COMBO` | Combo giá đặc biệt | Giá combo |

### debt_records.type
| Giá trị | Ý nghĩa |
|---------|---------|
| `BORROW` | Mua thiếu, tăng nợ |
| `REPAY` | Trả nợ, giảm nợ |
