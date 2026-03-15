# 🛒 Luồng Hoạt Động Bán Hàng (Staff / Client POS)

Đây là luồng chính mà nhân viên sử dụng hàng ngày để thực hiện giao dịch với khách hàng.

---

## 1. Luồng Bán Hàng Cơ Bản (Giao dịch Tiền mặt)
- **Bước 1:** Đăng nhập vào hệ thống.
- **Bước 2:** Mở màn hình **Bán hàng (POS)**.
- **Bước 3:** Quét mã vạch sản phẩm (hoặc tìm kiếm theo tên) -> Sản phẩm tự động thêm vào giỏ hàng.
- **Bước 4:** Điều chỉnh số lượng sản phẩm nếu cần.
- **Bước 5:** (Tùy chọn) Tìm kiếm khách hàng thân thiết bằng SĐT để tích điểm hoặc ghi nợ.
- **Bước 6:** Nhấn **Thanh toán (Checkout)**.
- **Bước 7:** Nhập số tiền khách đưa -> Hệ thống tính tiền thừa.
- **Bước 8:** Hoàn tất đơn hàng và in hóa đơn (Receipt).

## 2. Luồng Bán Hàng Ghi Nợ (Mua trước trả sau)
- **Bước 1:** Thêm sản phẩm vào giỏ hàng như bình thường.
- **Bước 2:** **Bắt buộc** phải chọn một Khách hàng có sẵn trong hệ thống.
- **Bước 3:** Tại phần thanh toán, chọn hình thức **Ghi nợ (Debt)**.
- **Bước 4:** Xác nhận hoàn tất. Tổng số tiền đơn hàng sẽ được cộng vào tài khoản nợ của khách hàng đó.

## 3. Xem Lịch Sử Đơn Hàng
- Nhân viên có thể xem lại các hóa đơn đã xuất trong ngày.
- Có thể in lại hóa đơn (Re-print) nếu khách hàng yêu cầu.
