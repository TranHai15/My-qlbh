-- ============================================================
-- Camellia Database Schema v1.1
-- Engine: SQLite 3
-- ============================================================

PRAGMA journal_mode=WAL;
PRAGMA foreign_keys=ON;

-- ------------------------------------------------------------
-- USERS — Quản lý tài khoản
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    username        TEXT    NOT NULL UNIQUE,
    password        TEXT    NOT NULL,
    display_name    TEXT,
    role            TEXT    NOT NULL DEFAULT 'CASHIER', -- 'ADMIN' | 'CASHIER'
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- Tạo tài khoản Admin mặc định: admin / admin123
INSERT OR IGNORE INTO users (username, password, display_name, role) VALUES
    ('admin', '$2a$10$I.OUwOqu.swTL4jLVNcYp.tK6YkY5RZQW7iygbjkMoKoQK1CDqKmm', 'Quản trị viên', 'ADMIN');

-- ------------------------------------------------------------
-- SETTINGS — Cấu hình hệ thống (Bỏ admin_password)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS settings (
    key         TEXT    PRIMARY KEY,
    value       TEXT    NOT NULL,
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

INSERT OR IGNORE INTO settings (key, value) VALUES
    ('shop_name',       'Camellia POS'),
    ('shop_address',    '123 Đường ABC, Quận 1, TP.HCM'),
    ('shop_phone',      '0123456789'),
    ('backup_path',     '');

-- ------------------------------------------------------------
-- CATEGORIES
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    description TEXT,
    parent_id   INTEGER REFERENCES categories(id),
    is_active   INTEGER NOT NULL DEFAULT 1,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- PRODUCTS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,
    barcode         TEXT    UNIQUE,
    category_id     INTEGER REFERENCES categories(id),
    cost_price      REAL    NOT NULL DEFAULT 0,
    sell_price      REAL    NOT NULL,
    stock_quantity  REAL    NOT NULL DEFAULT 0,
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    created_by      TEXT    NOT NULL DEFAULT 'system'
);

-- ------------------------------------------------------------
-- CUSTOMERS — Quản lý khách hàng
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,
    phone           TEXT    UNIQUE,
    address         TEXT,
    discount_rate   REAL    DEFAULT 0,
    reward_points   REAL    DEFAULT 0, -- Điểm tích lũy thưởng
    total_debt      REAL    DEFAULT 0,
    notes           TEXT,
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- PAYMENTS — Nhật ký thanh toán
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id        INTEGER NOT NULL REFERENCES orders(id),
    method          TEXT    NOT NULL DEFAULT 'CASH', -- 'CASH' | 'TRANSFER' | 'POINTS'
    amount_paid     REAL    NOT NULL DEFAULT 0,
    change_amount   REAL    NOT NULL DEFAULT 0,
    is_debt         INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- DEBT_RECORDS — Nhật ký công nợ
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS debt_records (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id     INTEGER NOT NULL REFERENCES customers(id),
    amount          REAL    NOT NULL,
    type            TEXT    NOT NULL, -- 'DEBT' (Nợ thêm) | 'REPAYMENT' (Trả nợ)
    description     TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- ORDERS — Đơn hàng
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    order_code      TEXT    NOT NULL UNIQUE,
    customer_id     INTEGER REFERENCES customers(id),
    subtotal        REAL    NOT NULL,
    discount_amount REAL    NOT NULL DEFAULT 0,
    total_amount    REAL    NOT NULL,
    status          TEXT    NOT NULL DEFAULT 'COMPLETED', -- 'COMPLETED' | 'CANCELLED'
    notes           TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- ------------------------------------------------------------
-- ORDER_ITEMS — Chi tiết đơn hàng
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id        INTEGER NOT NULL REFERENCES orders(id),
    product_id      INTEGER NOT NULL REFERENCES products(id),
    product_name    TEXT    NOT NULL,   -- snapshot
    unit_price      REAL    NOT NULL,   -- snapshot
    cost_price      REAL    NOT NULL,   -- snapshot
    quantity        REAL    NOT NULL,
    discount_amount REAL    NOT NULL DEFAULT 0,
    line_total      REAL    NOT NULL
);

-- ------------------------------------------------------------
-- AUDIT_LOG
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    action      TEXT    NOT NULL,
    target_type TEXT    NOT NULL,
    target_id   INTEGER,
    detail      TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);
