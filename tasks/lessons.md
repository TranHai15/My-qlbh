# Bài Học Kinh Nghiệm (Lessons Learned)

## 1. Truy cập Transaction từ Service
- **Lỗi:** Gọi `executeInTransaction` (protected) của `BaseRepository` từ Service ở package khác gây lỗi biên dịch.
- **Giải pháp:** Thêm phương thức `public runInTransaction` vào Repository để ủy quyền (delegate) thực thi transaction. Điều này vừa đảm bảo tính đóng gói của `BaseRepository` vừa cho phép Service điều phối transaction khi cần thiết.

## 2. Đồng bộ hóa Entity và Repository
- **Bài học:** Luôn kiểm tra kỹ các trường thông tin trong `BaseEntity` (như `createdAt`, `updatedAt`) để mapping đầy đủ trong Repository, đảm bảo dữ liệu không bị mất khi lưu trữ và truy xuất từ SQLite.

## 3. Quản lý công nợ Atomic
- **Bài học:** Việc ghi nợ/trả nợ phải luôn nằm trong một **Transaction** duy nhất. Logic bao gồm việc lưu lịch sử (`debt_records`) và cập nhật số dư khách hàng (`customers.total_debt`) phải được thực hiện cùng nhau, tránh tình trạng nợ ảo hoặc mất dấu dòng tiền.
