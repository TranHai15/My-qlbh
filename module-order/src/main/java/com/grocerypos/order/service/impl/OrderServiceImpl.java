package com.grocerypos.order.service.impl;

import com.grocerypos.core.event.AppEventBus;
import com.grocerypos.core.event.events.OrderCompletedEvent;
import com.grocerypos.core.exception.InsufficientStockException;
import com.grocerypos.core.exception.ResourceNotFoundException;
import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.core.util.DateTimeUtils;
import com.grocerypos.order.entity.Order;
import com.grocerypos.order.entity.OrderItem;
import com.grocerypos.order.entity.OrderStatus;
import com.grocerypos.order.model.Cart;
import com.grocerypos.order.model.CartItem;
import com.grocerypos.order.repository.OrderItemRepository;
import com.grocerypos.order.repository.OrderRepository;
import com.grocerypos.order.service.OrderService;
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

    public OrderServiceImpl(OrderRepository orderRepo, OrderItemRepository orderItemRepo, ProductService productService) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productService = productService;
    }

    @Override
    public Order createOrder(Cart cart, Long customerId, double discountAmount) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new ValidationException("Giỏ hàng trống, không thể tạo đơn hàng");
        }
        if (discountAmount < 0) {
            throw new ValidationException("Giảm giá không được là số âm");
        }

        return orderRepo.executeInTransaction(conn -> {
            double subtotal = cart.getSubtotal();
            double total = Math.max(0, subtotal - discountAmount);

            Order order = Order.builder()
                    .orderCode(generateOrderCode())
                    .customerId(customerId)
                    .subtotal(subtotal)
                    .discountAmount(discountAmount)
                    .totalAmount(total)
                    .status(OrderStatus.COMPLETED)
                    .build();

            order = orderRepo.save(order);
            List<OrderItem> savedItems = new ArrayList<>();

            for (CartItem cartItem : cart.getItems()) {
                Product product = productService.findById(cartItem.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + cartItem.getProduct().getName()));

                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new InsufficientStockException(
                            "Không đủ hàng: " + product.getName() + " (còn " + product.getStockQuantity() + ")");
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

                savedItems.add(orderItemRepo.save(item));
                productService.updateStock(product.getId(), -cartItem.getQuantity());
            }

            order.setItems(savedItems);
            log.info("Tạo đơn hàng thành công: {}, Tổng: {}", order.getOrderCode(), total);
            AppEventBus.post(new OrderCompletedEvent(order.getId(), total));
            return order;
        });
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
        String dateStr = LocalDate.now().toString().replace("-", ""); // YYYYMMDD
        int sequence = orderRepo.countToday() + 1;
        return String.format("ORD-%s-%04d", dateStr, sequence);
    }
}
