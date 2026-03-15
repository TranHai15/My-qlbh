# 🧪 Kịch Bản Kiểm Thử (Test Scenarios)

Bạn hãy thử thực hiện các kịch bản sau để kiểm tra tính ổn định của ứng dụng.

---

### Kịch bản 1: "Cửa hàng mới khai trương" (Admin)
1. Tạo danh mục: `Nước giải khát`.
2. Tạo sản phẩm: `Coca Cola`, Barcode: `123456`, Giá bán: `10.000đ`, Tồn kho: `0`.
3. Tạo Nhà cung cấp: `Đại lý Toàn Cầu`.
4. Thực hiện Nhập kho: Nhập `50` lon Coca Cola từ `Đại lý Toàn Cầu`.
5. **Kết quả mong đợi:** Quay lại màn hình Sản phẩm, tồn kho Coca Cola phải hiển thị là `50`.

### Kịch bản 2: "Khách hàng mua lẻ" (Staff)
1. Mở POS, quét mã `123456` (hoặc tìm Coca Cola).
2. Thêm `2` lon vào giỏ hàng.
3. Không chọn khách hàng (Khách vãng lai).
4. Nhấn Thanh toán, nhập khách đưa `50.000đ`.
5. **Kết quả mong đợi:** Hệ thống báo tiền thừa `30.000đ`, đơn hàng được lưu, tồn kho Coca giảm xuống còn `48`.

### Kịch bản 3: "Khách quen mua nợ" (Staff)
1. Tạo khách hàng: `Anh Tú`, SĐT: `0909123456`.
2. Mở POS, chọn `5` lon Coca Cola.
3. Tìm và chọn khách hàng `Anh Tú`.
4. Chọn thanh toán bằng **Ghi nợ (Debt)**.
5. **Kết quả mong đợi:** Đơn hàng hoàn tất. Vào phần Quản lý khách hàng, kiểm tra `Anh Tú` phải có khoản nợ là `50.000đ`.

### Kịch bản 4: "Kiểm tra bảo mật"
1. Thử đăng nhập sai mật khẩu.
2. Thử bán hàng khi sản phẩm trong kho đã hết (Tồn kho = 0).
3. **Kết quả mong đợi:** Hệ thống phải có thông báo lỗi hoặc cảnh báo phù hợp.
