# 🛠️ Luồng Hoạt Động Của Quản Trị Viên (Admin)

Quản trị viên là người thiết lập "xương sống" cho cửa hàng trước khi nhân viên có thể bán hàng.

---

## 1. Luồng Thiết Lập Sản Phẩm (Setup)
Mục đích: Tạo danh mục và sản phẩm để có dữ liệu bán hàng.
- **Bước 1:** Vào menu **Danh mục (Category)** -> Thêm mới các loại hàng (ví dụ: Bánh kẹo, Nước giải khát, Gia vị).
- **Bước 2:** Vào menu **Sản phẩm (Product)** -> Thêm sản phẩm mới.
    - Nhập mã vạch (Barcode) để sau này quét tại quầy POS.
    - Chọn danh mục tương ứng.
    - Nhập giá vốn (Cost Price) và giá bán (Sell Price).
    - Thiết lập số lượng tồn kho ban đầu (hoặc để bằng 0 để nhập kho sau).

## 2. Luồng Quản Lý Kho (Inventory)
Mục đích: Nhập hàng từ nhà cung cấp và tăng số lượng tồn kho.
- **Bước 1:** Vào menu **Nhà cung cấp (Supplier)** -> Thêm thông tin đại lý/công ty cung cấp hàng.
- **Bước 2:** Vào menu **Nhập kho (Stock Entry)**.
    - Chọn sản phẩm cần nhập.
    - Chọn nhà cung cấp.
    - Nhập số lượng và đơn giá nhập thực tế.
    - Hệ thống sẽ tự động cộng dồn vào `stock_quantity` của sản phẩm.

## 3. Luồng Quản Lý Khách Hàng & Công Nợ
- **Bước 1:** Thêm khách hàng mới (Tên, Số điện thoại).
- **Bước 2:** Theo dõi **Công nợ (Debt)**: Nếu khách hàng mua nợ tại quầy POS, Admin có thể vào đây để xem tổng nợ và ghi nhận các đợt khách trả tiền nợ.

## 4. Báo Cáo & Thống Kê
- Xem doanh thu theo ngày/tháng.
- Xem danh sách sản phẩm sắp hết hàng (Low Stock) để kịp thời nhập thêm.
