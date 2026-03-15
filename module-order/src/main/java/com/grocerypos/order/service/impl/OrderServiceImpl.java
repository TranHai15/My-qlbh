package com.grocerypos.order.service.impl;

import com.grocerypos.core.event.AppEventBus;
import com.grocerypos.core.event.events.OrderCompletedEvent;
import com.grocerypos.core.exception.InsufficientStockException;
import com.grocerypos.core.exception.ResourceNotFoundException;
import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.customer.service.CustomerService;
import com.grocerypos.order.entity.Order;
import com.grocerypos.order.entity.OrderItem;
import com.grocerypos.order.entity.OrderStatus;
import com.grocerypos.order.model.Cart;
import com.grocerypos.order.model.CartItem;
import com.grocerypos.order.repository.OrderItemRepository;
import com.grocerypos.order.repository.OrderRepository;
import com.grocerypos.order.service.OrderService;
import com.grocerypos.order.service.PaymentService;
import com.grocerypos.product.entity.Product;
import com.grocerypos.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final ProductService productService;
    private final CustomerService customerService;
    private PaymentService paymentService;

    public OrderServiceImpl(OrderRepository orderRepo, OrderItemRepository orderItemRepo, ProductService productService, CustomerService customerService) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productService = productService;
        this.customerService = customerService;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Order checkout(Cart cart, Long customerId, double discountAmount, double pointsToUse, double amountPaid) {
        log.info("[DEBUG] [CHECKOUT] Bắt đầu luồng Checkout. Khách: {}, Điểm dùng: {}, Tiền trả: {}", customerId, pointsToUse, amountPaid);
        
        Order savedOrder;
        try {
            savedOrder = orderRepo.executeInTransaction(conn -> {
                log.info("[DEBUG] [CHECKOUT] Đang trong Transaction. Bước 1: Gọi createOrderInternal");
                Order order = createOrderInternal(cart, customerId, discountAmount, pointsToUse);
                
                if (paymentService != null) {
                    log.info("[DEBUG] [CHECKOUT] Bước 2: Gọi paymentService.processPayment");
                    paymentService.processPayment(order.getId(), amountPaid, customerId);
                } else {
                    log.warn("[DEBUG] [CHECKOUT] Cảnh báo: paymentService null, bỏ qua thanh toán!");
                }
                return order;
            });
            log.info("[DEBUG] [CHECKOUT] Transaction hoàn tất thành công. ORD: {}", savedOrder.getOrderCode());
        } catch (Exception e) {
            log.error("[DEBUG] [CHECKOUT] LỖI trong quá trình Transaction!", e);
            throw e;
        }

        log.info("[DEBUG] [CHECKOUT] Đang phát sự kiện OrderCompletedEvent...");
        AppEventBus.post(new OrderCompletedEvent(savedOrder.getId(), savedOrder.getTotalAmount(), customerId));
        log.info("[DEBUG] [CHECKOUT] Kết thúc phương thức checkout.");
        
        return savedOrder;
    }

    private Order createOrderInternal(Cart cart, Long customerId, double discountAmount, double pointsToUse) {
        log.info("[DEBUG] [CREATE-ORDER] Bắt đầu createOrderInternal...");
        
        double pointDiscount = pointsToUse * 1000;
        double subtotal = cart.getSubtotal();
        double total = Math.max(0, subtotal - discountAmount - pointDiscount);

        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .customerId(customerId)
                .subtotal(subtotal)
                .discountAmount(discountAmount + pointDiscount)
                .totalAmount(total)
                .status(OrderStatus.COMPLETED)
                .build();
        
        // Cần gán thủ công vì Builder không gọi constructor của BaseEntity
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setUpdatedAt(java.time.LocalDateTime.now());

        log.info("[DEBUG] [CREATE-ORDER] Đang lưu Header Order...");
        order = orderRepo.save(order);
        
        if (customerId != null && pointsToUse > 0) {
            log.info("[DEBUG] [CREATE-ORDER] Đang trừ điểm khách hàng: -{}", pointsToUse);
            customerService.updatePoints(customerId, -pointsToUse);
        }

        List<OrderItem> items = new ArrayList<>();
        int count = 0;
        for (CartItem cartItem : cart.getItems()) {
            count++;
            Product product = productService.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + cartItem.getProduct().getName()));

            log.info("[DEBUG] [CREATE-ORDER] Xử lý mặt hàng #{}: {}, SL: {}", count, product.getName(), cartItem.getQuantity());
            
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Không đủ hàng: " + product.getName());
            }

            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(cartItem.getUnitPrice())
                    .costPrice(product.getCostPrice())
                    .quantity(cartItem.getQuantity())
                    .discountAmount(cartItem.getItemDiscount())
                    .lineTotal(cartItem.getLineTotal())
                    .build();

            orderItemRepo.save(item);
            productService.updateStock(product.getId(), -cartItem.getQuantity());
            items.add(item);
        }

        order.setItems(items);
        log.info("[DEBUG] [CREATE-ORDER] Hoàn tất tạo đơn hàng với {} mặt hàng.", items.size());
        return order;
    }

    @Override
    public Order createOrder(Cart cart, Long customerId, double discountAmount, double pointsToUse) {
        return checkout(cart, customerId, discountAmount, pointsToUse, cart.getSubtotal());
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepo.findById(id);
    }

    @Override
    public Optional<Order> findByCode(String code) {
        List<Order> orders = orderRepo.search(code, null);
        return orders.stream().filter(o -> o.getOrderCode().equalsIgnoreCase(code)).findFirst();
    }

    @Override
    public List<Order> findAll() {
        return orderRepo.findAll();
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepo.findByOrderId(orderId);
    }

    @Override
    public List<Order> searchOrders(String query, LocalDate date) {
        return orderRepo.search(query, date);
    }

    private String generateOrderCode() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        int sequence = orderRepo.countToday() + 1;
        return String.format("ORD-%s-%04d", dateStr, sequence);
    }
}
