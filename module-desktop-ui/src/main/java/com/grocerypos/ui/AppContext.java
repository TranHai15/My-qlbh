package com.grocerypos.ui;

import com.grocerypos.core.event.AppEventBus;
import com.grocerypos.customer.event.OrderEventListener;
import com.grocerypos.core.repository.AuthRepository;
import com.grocerypos.core.service.AuthService;
import com.grocerypos.core.service.impl.AuthServiceImpl;
import com.grocerypos.product.repository.CategoryRepository;
import com.grocerypos.product.repository.ProductRepository;
import com.grocerypos.product.service.CategoryService;
import com.grocerypos.product.service.ProductService;
import com.grocerypos.product.service.impl.CategoryServiceImpl;
import com.grocerypos.product.service.impl.ProductServiceImpl;
import com.grocerypos.customer.repository.CustomerRepository;
import com.grocerypos.customer.service.CustomerService;
import com.grocerypos.customer.service.impl.CustomerServiceImpl;
import com.grocerypos.order.repository.OrderItemRepository;
import com.grocerypos.order.repository.OrderRepository;
import com.grocerypos.order.repository.PaymentRepository;
import com.grocerypos.order.service.OrderService;
import com.grocerypos.order.service.PaymentService;
import com.grocerypos.order.service.impl.OrderServiceImpl;
import com.grocerypos.order.service.impl.PaymentServiceImpl;
import com.grocerypos.promotion.service.DiscountEngine;
import com.grocerypos.promotion.service.impl.DiscountEngineImpl;
import com.grocerypos.inventory.repository.StockEntryRepository;
import com.grocerypos.inventory.repository.SupplierRepository;
import com.grocerypos.inventory.service.InventoryService;
import com.grocerypos.inventory.service.impl.InventoryServiceImpl;
import com.grocerypos.report.service.ReportService;
import com.grocerypos.report.service.impl.ReportServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AppContext {
    private static final Logger log = LoggerFactory.getLogger(AppContext.class);
    private static final Map<Class<?>, Object> registry = new HashMap<>();

    public static void initialize() {
        log.info("Bắt đầu khởi tạo AppContext (Reporting Version)...");
        try {
            // 1. Repositories
            AuthRepository authRepo = new AuthRepository();
            CategoryRepository categoryRepo = new CategoryRepository();
            ProductRepository productRepo = new ProductRepository();
            CustomerRepository customerRepo = new CustomerRepository();
            OrderRepository orderRepo = new OrderRepository();
            OrderItemRepository orderItemRepo = new OrderItemRepository();
            PaymentRepository paymentRepo = new PaymentRepository();
            StockEntryRepository stockEntryRepo = new StockEntryRepository();
            SupplierRepository supplierRepo = new SupplierRepository();

            // 2. Services
            AuthService authService = new AuthServiceImpl(authRepo);
            CategoryService categoryService = new CategoryServiceImpl(categoryRepo);
            ProductService productService = new ProductServiceImpl(productRepo);
            CustomerService customerService = new CustomerServiceImpl(customerRepo);
            OrderService orderService = new OrderServiceImpl(orderRepo, orderItemRepo, productService, customerService);
            
            // PaymentService dùng trực tiếp CustomerRepository để tích điểm
            PaymentService paymentService = new PaymentServiceImpl(paymentRepo, orderService, customerRepo);
            
            // Link PaymentService vào OrderService để hỗ trợ Checkout gộp
            ((com.grocerypos.order.service.impl.OrderServiceImpl)orderService).setPaymentService(paymentService);
            
            DiscountEngine discountEngine = new DiscountEngineImpl();
            InventoryService inventoryService = new InventoryServiceImpl(stockEntryRepo, supplierRepo, productRepo);
            ReportService reportService = new ReportServiceImpl();

            // 3. Register
            register(AuthService.class, authService);
            register(CategoryService.class, categoryService);
            register(ProductService.class, productService);
            register(CustomerService.class, customerService);
            register(OrderService.class, orderService);
            register(PaymentService.class, paymentService);
            register(DiscountEngine.class, discountEngine);
            register(InventoryService.class, inventoryService);
            register(ReportService.class, reportService);

            // 4. Register Event Listeners
            AppEventBus.register(new OrderEventListener(customerService));

            log.info("Khởi tạo AppContext thành công.");
        } catch (Exception e) {
            log.error("Lỗi khởi tạo AppContext", e);
            throw new RuntimeException(e);
        }
    }

    public static <T> void register(Class<T> type, T instance) { registry.put(type, instance); }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        T instance = (T) registry.get(type);
        if (instance == null) throw new IllegalStateException("Service not found: " + type.getName());
        return instance;
    }
}
