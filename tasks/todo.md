# Nhiệm vụ: Triển khai Module Quản lý Khách hàng (module-customer)

## 1. Chuẩn bị cấu trúc
- [x] Tạo package `com.grocerypos.customer` và các sub-packages.

## 2. Thực thể (Entities)
- [x] Tạo `Customer.java`.
- [x] Tạo `DebtRecord.java`.
- [x] Tạo `DebtType.java`.

## 3. Kho lưu trữ (Repositories)
- [x] Tạo `CustomerRepository.java`.
- [x] Tạo `DebtRepository.java`.

## 4. Dịch vụ (Services)
- [x] Tạo `CustomerService.java` & `DebtService.java` (Interfaces).
- [x] Tạo `CustomerServiceImpl.java` & `DebtServiceImpl.java`.

## 5. Kiểm tra và Hoàn thiện
- [x] Viết Unit Test cho `CustomerService` và `DebtService`.
- [x] Cập nhật `tasks/lessons.md`.

## Kết quả
- Module hoạt động ổn định, các chức năng CRUD khách hàng và quản lý công nợ đã được kiểm chứng.
- Đảm bảo tính Atomic cho các giao dịch liên quan đến tiền bạc.
