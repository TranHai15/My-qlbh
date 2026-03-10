# SKILL — module-core

## Vai Trò Của Bạn
Bạn đang implement `module-core` cho dự án GroceryPOS.  
Đây là **nền tảng của toàn bộ hệ thống** — làm đúng, làm kỹ, mọi người mới bắt đầu được.

## Trước Khi Code — Đọc Bắt Buộc
1. `docs/01-ARCHITECTURE.md` — mục 3 (Nguyên Tắc Kiến Trúc)
2. `docs/02-DATABASE-SCHEMA.md` — toàn bộ schema SQL
3. `docs/04-CODING-STANDARDS.md` — toàn bộ

---

## Các File Cần Tạo (theo thứ tự)

### Bước 1: AppConfig + DatabaseConfig
```
module-core/src/main/java/com/grocerypos/core/config/AppConfig.java
module-core/src/main/java/com/grocerypos/core/config/DatabaseConfig.java
```

**AppConfig** — đọc file `application.properties` (đặt tại `resources/`):
```properties
# application.properties
db.path=~/.grocerypos/grocerypos.db
shop.name=Tạp Hóa Của Tôi
shop.address=
shop.phone=
```
```java
public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class
                .getResourceAsStream("/application.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new AppException("Không tìm thấy application.properties", e);
        }
    }

    public static String get(String key) { return props.getProperty(key, ""); }
    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
```

**DatabaseConfig** — khởi tạo HikariCP connection pool:
```java
public class DatabaseConfig {
    private static HikariDataSource dataSource;

    public static void initialize() {
        String dbPath = resolveDbPath(AppConfig.get("db.path", "~/.grocerypos/grocerypos.db"));
        createDirectoryIfNeeded(dbPath);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbPath);
        config.setMaximumPoolSize(5);
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("foreign_keys", "ON");

        dataSource = new HikariDataSource(config);
        runSchema();  // chạy schema.sql lần đầu
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private static void runSchema() { /* đọc và chạy schema.sql */ }
}
```

---

### Bước 2: BaseEntity + BaseRepository
```
module-core/src/main/java/com/grocerypos/core/base/BaseEntity.java
module-core/src/main/java/com/grocerypos/core/base/BaseRepository.java
```

**BaseEntity** — tự lấy `createdBy` từ SessionManager:
```java
@Data
public abstract class BaseEntity {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    protected BaseEntity() {
        this.createdAt  = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
        // Tự động gán người tạo từ session hiện tại
        this.createdBy  = SessionManager.getInstance()
                              .getCurrentUsername()
                              .orElse("system");
    }
}
```

**BaseRepository** — abstract class, helper JDBC:
```java
public abstract class BaseRepository {
    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) { ... }
    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) { ... }
    protected long insert(String sql, Object... params) { ... }
    protected int update(String sql, Object... params) { ... }
    protected <T> T executeInTransaction(Supplier<T> task) { ... } // auto commit/rollback
}
```

---

### Bước 3: SessionManager
```
module-core/src/main/java/com/grocerypos/core/session/SessionManager.java
module-core/src/main/java/com/grocerypos/core/session/UserSession.java
```

```java
// UserSession — DTO lưu thông tin phiên
@Data @Builder
public class UserSession {
    private Long userId;
    private String username;
    private String displayName;
    private UserRole role;          // ADMIN | CASHIER
    private LocalDateTime loginTime;
}

public enum UserRole { ADMIN, CASHIER }

// SessionManager — Singleton
public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private UserSession currentSession;

    private SessionManager() {}

    public static SessionManager getInstance() { return INSTANCE; }

    public void login(UserSession session) {
        this.currentSession = session;
        log.info("Đăng nhập: {} ({})", session.getUsername(), session.getRole());
    }

    public void logout() {
        log.info("Đăng xuất: {}", currentSession != null ? currentSession.getUsername() : "?");
        this.currentSession = null;
    }

    public Optional<UserSession> getCurrentSession() {
        return Optional.ofNullable(currentSession);
    }

    public Optional<String> getCurrentUsername() {
        return getCurrentSession().map(UserSession::getUsername);
    }

    public boolean isLoggedIn() { return currentSession != null; }

    public boolean isAdmin() {
        return getCurrentSession()
            .map(s -> s.getRole() == UserRole.ADMIN)
            .orElse(false);
    }
}
```

---

### Bước 4: Exceptions
```
module-core/src/main/java/com/grocerypos/core/exception/
├── AppException.java
├── ValidationException.java
├── ResourceNotFoundException.java    ← (không phải NotFoundException)
└── InsufficientStockException.java
```

```java
// AppException — base
public class AppException extends RuntimeException {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }
}

// ResourceNotFoundException
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) { super(message); }
}
// Dùng: throw new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + id);
```

---

### Bước 5: EventBus (Guava)
```
module-core/src/main/java/com/grocerypos/core/event/AppEventBus.java
module-core/src/main/java/com/grocerypos/core/event/AppEvent.java
module-core/src/main/java/com/grocerypos/core/event/events/
├── OrderCompletedEvent.java
├── LowStockEvent.java
└── DebtRecordedEvent.java
```

Dùng **Guava EventBus** thay vì tự viết:
```java
public class AppEventBus {
    private static final EventBus BUS = new EventBus("GroceryPOS");

    public static void publish(Object event) {
        BUS.post(event);
    }

    public static void subscribe(Object listener) {
        BUS.register(listener);    // listener dùng @Subscribe annotation
    }

    public static void unsubscribe(Object listener) {
        BUS.unregister(listener);
    }
}

// Cách dùng trong module khác:
// AppEventBus.subscribe(this);   // đăng ký listener
// @Subscribe public void onOrderCompleted(OrderCompletedEvent e) { ... }
```

---

### Bước 6: Utils
```
module-core/src/main/java/com/grocerypos/core/util/
├── MoneyUtils.java       → formatVND(), roundMoney()
├── DateTimeUtils.java    → format chuẩn VN (dd/MM/yyyy HH:mm)
├── PasswordUtils.java    → BCrypt hash + verify
├── StringUtils.java      → normalize, removeDiacritics(), isBlank()
└── BarcodeUtils.java     → generate(), validate(), createImage()
```

**PasswordUtils** — BCrypt bắt buộc:
```java
public class PasswordUtils {
    private static final int BCRYPT_ROUNDS = 10;

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
```

**StringUtils** — CHỈ chứa text utilities (KHÔNG tạo mã hóa đơn):
```java
public class StringUtils {
    // Bỏ dấu tiếng Việt → dùng cho tìm kiếm không phân biệt dấu
    public static String removeDiacritics(String input) { ... }

    // Chuẩn hóa keyword tìm kiếm
    public static String normalizeSearch(String input) {
        return removeDiacritics(input).toLowerCase().trim();
    }

    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
// ⚠️ Mã hóa đơn (ORD-20240115-0001) thuộc về module-order, KHÔNG đặt ở đây
```

---

### Bước 7: schema.sql
```
module-desktop-ui/src/main/resources/db/schema.sql
```
Copy toàn bộ từ `docs/02-DATABASE-SCHEMA.md`

---

## build.gradle cho module-core

```groovy
dependencies {
    // Database
    implementation 'org.xerial:sqlite-jdbc:3.45.1.0'
    implementation 'com.zaxxer:HikariCP:5.1.0'

    // Guava EventBus
    implementation 'com.google.guava:guava:33.1.0-jre'

    // BCrypt cho PasswordUtils
    implementation 'org.mindrot:jbcrypt:0.4'

    // Barcode
    implementation 'com.google.zxing:core:3.5.3'
    implementation 'com.google.zxing:javase:3.5.3'

    // Logging
    implementation 'org.slf4j:slf4j-api:2.0.12'
    implementation 'ch.qos.logback:logback-classic:1.5.3'

    // Lombok
    compileOnly     'org.projectlombok:lombok:1.18.32'
    annotationProcessor 'org.projectlombok:lombok:1.18.32'

    // Test
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
}
```

---

## Checklist Hoàn Thành

- [ ] `DatabaseConfig.initialize()` tạo DB + chạy schema.sql thành công
- [ ] `AppConfig.get("shop.name")` đọc đúng từ `application.properties`
- [ ] `BaseRepository.executeInTransaction()` rollback đúng khi có exception
- [ ] `SessionManager.login()` / `logout()` / `isLoggedIn()` hoạt động đúng
- [ ] `BaseEntity` tự gán `createdBy` từ SessionManager khi khởi tạo
- [ ] `AppEventBus.publish()` + `@Subscribe` hoạt động đúng (Guava)
- [ ] `MoneyUtils.formatVND(150000)` → `"150.000 ₫"`
- [ ] `PasswordUtils.verify()` đúng với password đã hash
- [ ] `StringUtils.removeDiacritics("Sữa tươi")` → `"Sua tuoi"`
- [ ] `BarcodeUtils.generate()` tạo mã không trùng
- [ ] Tất cả Utils có unit test
- [ ] Không có dependency đến module khác ngoài core

## Báo Cáo Hoàn Thành
Sau khi xong, thông báo: "module-core DONE — team có thể bắt đầu nhóm 1"
