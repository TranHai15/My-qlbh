DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM stock_entries;

INSERT OR IGNORE INTO categories (id, name, description) VALUES (1, 'Thực phẩm', 'Mì tôm, bánh kẹo, đồ khô');
INSERT OR IGNORE INTO categories (id, name, description) VALUES (2, 'Đồ uống', 'Nước ngọt, bia, sữa');
INSERT OR IGNORE INTO categories (id, name, description) VALUES (3, 'Hóa mỹ phẩm', 'Bột giặt, dầu gội');
INSERT OR IGNORE INTO categories (id, name, description) VALUES (4, 'Đồ gia dụng', 'Chổi, bát đĩa');

INSERT OR IGNORE INTO suppliers (id, name, phone, address) VALUES (1, 'Công ty Vinamilk', '0901234567', 'TP. Hồ Chí Minh');
INSERT OR IGNORE INTO suppliers (id, name, phone, address) VALUES (2, 'Đại lý Tổng hợp Minh Anh', '0912345678', 'Hà Nội');
INSERT OR IGNORE INTO suppliers (id, name, phone, address) VALUES (3, 'Unilever Việt Nam', '0988888888', 'Bình Dương');

INSERT OR IGNORE INTO products (id, name, barcode, category_id, cost_price, sell_price, stock_quantity, is_active) VALUES (1, 'Sữa tươi Vinamilk 180ml', '8934001', 2, 6500, 8000, 100, 1);
INSERT OR IGNORE INTO products (id, name, barcode, category_id, cost_price, sell_price, stock_quantity, is_active) VALUES (2, 'Mì Hảo Hảo Tôm Chua Cay', '8934002', 1, 3200, 4500, 200, 1);
INSERT OR IGNORE INTO products (id, name, barcode, category_id, cost_price, sell_price, stock_quantity, is_active) VALUES (3, 'Nước khoáng Lavie 500ml', '8934003', 2, 4000, 6000, 50, 1);
INSERT OR IGNORE INTO products (id, name, barcode, category_id, cost_price, sell_price, stock_quantity, is_active) VALUES (4, 'Bột giặt OMO 800g', '8934004', 3, 35000, 42000, 20, 1);
INSERT OR IGNORE INTO products (id, name, barcode, category_id, cost_price, sell_price, stock_quantity, is_active) VALUES (5, 'Dầu ăn Simply 1L', '8934005', 1, 42000, 50000, 15, 1);
INSERT OR IGNORE INTO products (id, name, barcode, category_id, cost_price, sell_price, stock_quantity, is_active) VALUES (6, 'Coca Cola 330ml', '8934006', 2, 7000, 10000, 5, 1);
INSERT OR IGNORE INTO products (id, name, barcode, category_id, cost_price, sell_price, stock_quantity, is_active) VALUES (7, 'Nước rửa bát Sunlight', '8934007', 3, 18000, 25000, 30, 1);

INSERT OR IGNORE INTO customers (id, name, phone, address, is_active) VALUES (1, 'Nguyễn Văn An', '0971112223', 'Quận 1, HCM', 1);
INSERT OR IGNORE INTO customers (id, name, phone, address, is_active) VALUES (2, 'Trần Thị Bình', '0972223334', 'Quận 3, HCM', 1);
INSERT OR IGNORE INTO customers (id, name, phone, address, is_active) VALUES (3, 'Lê Văn Cường', '0973334445', 'Quận Thủ Đức', 1);

-- Sử dụng order_code để mapping thay vì ID cứng
INSERT INTO orders (order_code, customer_id, subtotal, discount_amount, total_amount, status, created_at) VALUES ('ORD-001', 1, 150000, 10000, 140000, 'COMPLETED', datetime('now', '-10 days'));
INSERT INTO order_items (order_id, product_id, product_name, unit_price, cost_price, quantity, discount_amount, line_total) SELECT id, 1, 'Sữa tươi Vinamilk 180ml', 8000, 6500, 10, 0, 80000 FROM orders WHERE order_code='ORD-001';
INSERT INTO order_items (order_id, product_id, product_name, unit_price, cost_price, quantity, discount_amount, line_total) SELECT id, 2, 'Mì Hảo Hảo Tôm Chua Cay', 4500, 3200, 15, 0, 67500 FROM orders WHERE order_code='ORD-001';

INSERT INTO orders (order_code, customer_id, subtotal, discount_amount, total_amount, status, created_at) VALUES ('ORD-002', 2, 300000, 0, 300000, 'COMPLETED', datetime('now', '-5 days'));
INSERT INTO order_items (order_id, product_id, product_name, unit_price, cost_price, quantity, discount_amount, line_total) SELECT id, 4, 'Bột giặt OMO 800g', 42000, 35000, 5, 0, 210000 FROM orders WHERE order_code='ORD-002';
INSERT INTO order_items (order_id, product_id, product_name, unit_price, cost_price, quantity, discount_amount, line_total) SELECT id, 5, 'Dầu ăn Simply 1L', 50000, 42000, 2, 0, 100000 FROM orders WHERE order_code='ORD-002';

INSERT INTO orders (order_code, customer_id, subtotal, discount_amount, total_amount, status, created_at) VALUES ('ORD-003', NULL, 85000, 5000, 80000, 'COMPLETED', datetime('now'));
INSERT INTO order_items (order_id, product_id, product_name, unit_price, cost_price, quantity, discount_amount, line_total) SELECT id, 6, 'Coca Cola 330ml', 10000, 7000, 5, 0, 50000 FROM orders WHERE order_code='ORD-003';
INSERT INTO order_items (order_id, product_id, product_name, unit_price, cost_price, quantity, discount_amount, line_total) SELECT id, 7, 'Nước rửa bát Sunlight', 25000, 18000, 1, 0, 25000 FROM orders WHERE order_code='ORD-003';

INSERT INTO orders (order_code, customer_id, subtotal, discount_amount, total_amount, status, created_at) VALUES ('ORD-004', 3, 450000, 0, 450000, 'COMPLETED', datetime('now', '-2 days'));
INSERT INTO order_items (order_id, product_id, product_name, unit_price, cost_price, quantity, discount_amount, line_total) SELECT id, 2, 'Mì Hảo Hảo Tôm Chua Cay', 4500, 3200, 100, 0, 450000 FROM orders WHERE order_code='ORD-004';
