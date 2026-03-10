# 📓 Bài học rút ra (Lessons Learned)

## Module-core Implementation
- **Gradle Configuration:** Thuộc tính `rootProject.name` và lệnh `include` chỉ nên được khai báo trong `settings.gradle`. Khai báo trong `build.gradle` sẽ gây lỗi "read-only property".
- **Empty Modules:** Gradle yêu cầu các thư mục module được khai báo trong `settings.gradle` phải tồn tại thực tế trên ổ đĩa, nếu không sẽ báo lỗi trong quá trình cấu hình (configuration phase).
- **Java Toolchain:** Nếu Gradle toolchain không tìm thấy phiên bản Java yêu cầu (ví dụ: 17) trong môi trường Linux, cần cấu hình toolchain repository hoặc cập nhật version trong `build.gradle` khớp với JVM đang chạy (ví dụ: 21).
- **SQLite Database Path:** Việc tự động xác định đường dẫn DB theo OS giúp ứng dụng hoạt động ổn định trên nhiều nền tảng mà không cần cấu hình thủ công.
