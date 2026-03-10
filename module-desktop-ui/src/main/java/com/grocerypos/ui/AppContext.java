package com.grocerypos.ui;

import com.grocerypos.core.repository.AuthRepository;
import com.grocerypos.core.service.AuthService;
import com.grocerypos.core.service.impl.AuthServiceImpl;
import java.util.HashMap;
import java.util.Map;

/**
 * Dependency Injection thủ công theo thiết kế UI-GUIDE.md.
 */
public class AppContext {
    private static final Map<Class<?>, Object> registry = new HashMap<>();

    public static void initialize() {
        // Khởi tạo Repositories
        AuthRepository authRepo = new AuthRepository();

        // Khởi tạo Services
        AuthService authService = new AuthServiceImpl(authRepo);

        // Đăng ký Services
        register(AuthService.class, authService);
    }

    public static <T> void register(Class<T> type, T instance) {
        registry.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        T instance = (T) registry.get(type);
        if (instance == null) throw new IllegalStateException("Service not found: " + type.getName());
        return instance;
    }
}
