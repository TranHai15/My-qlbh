# 📋 Lộ trình triển khai module-core

- [x] **Bước 1: Cấu hình Gradle & Khởi tạo cấu trúc thư mục**
    - [x] Cập nhật `settings.gradle`.
    - [x] Tạo `module-core/build.gradle`.
    - [x] Tạo cấu trúc package `com.grocerypos.core`.

- [x] **Bước 2: Triển khai Base Classes**
    - [x] `BaseEntity.java`: Chứa các trường id, createdAt, updatedAt.
    - [x] `BaseRepository.java`: Cung cấp các helper methods cho JDBC/HikariCP.

- [x] **Bước 3: Triển khai Exception System**
    - [x] `AppException.java` (Base).
    - [x] `ValidationException.java`.
    - [x] `ResourceNotFoundException.java`.

- [x] **Bước 4: Triển khai Utils (Tiện ích)**
    - [x] `MoneyUtils.java`, `DateTimeUtils.java`, `PasswordUtils.java`, `StringUtils.java`.

- [x] **Bước 5: Cấu hình Hệ thống (Config)**
    - [x] `DatabaseConfig.java`: Khởi tạo HikariCP và kết nối SQLite.
    - [x] `EventBus.java`: Wrapper cho Guava EventBus để giao tiếp giữa các module.

- [x] **Bước 6: Quản lý Phiên làm việc (Session)**
    - [x] `UserSession.java` và `SessionManager.java`.

- [x] **Bước 7: Kiểm chứng (Verification)**
    - [x] Chạy `./gradlew :module-core:build`.

- [ ] **Bước 8: Triển khai AuthService (Đăng nhập)**
    - [ ] `AuthService.java`: Interface định nghĩa login/logout.
    - [ ] `AuthRepository.java`: Truy vấn mật khẩu admin từ bảng `settings`.
    - [ ] `AuthServiceImpl.java`: Logic kiểm tra mật khẩu và gọi SessionManager.
    - [ ] `AuthServiceTest.java`: Unit test cho chức năng đăng nhập.
