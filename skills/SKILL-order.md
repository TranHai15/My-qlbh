# SKILL — module-order

## Vai Trò Của Bạn
Bạn implement `module-order` — trái tim của hệ thống POS: tạo đơn hàng, thanh toán, ghi nợ.

## Điều Kiện Tiên Quyết (PHẢI XONG TRƯỚC)
- `module-core` ✅
- `module-product` ✅
- `module-customer` ✅
- `module-promotion` ✅

## Đọc Bắt Buộc
- `docs/02-DATABASE-SCHEMA.md` — bảng: `orders`, `order_items`, `payments`
- `docs/03-API-CONTRACTS.md` — mục OrderService, PaymentService, Cart, CartItem
- `docs/04-CODING-STANDARDS.md`
- `docs/05-MODULE-GUIDE.md` — mục Module Order

---

## Package Structure

```
module-order/src/main/java/com/grocerypos/order/
├── entity/
│   ├── Order.java
│   ├── OrderItem.java
│   └── Payment.java
├── model/
│   ├── Cart.java           ← In-memory, không lưu DB
│   ├── CartItem.java
│   └── DiscountResult.java
├── repository/
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   └── PaymentRepository.java
└── service/
    ├── OrderService.java       ← Interface
    ├── PaymentService.java     ← Interface
    └── impl/
        ├── OrderServiceImpl.java
        └── PaymentServiceImpl.java
```

---

## Entity Spec

### Order.java
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Order extends BaseEntity {
    private String orderCode;       // ORD-20240115-001
    private Long customerId;        // null = khách vãng lai
    private double subtotal;        // trước giảm giá
    private double discountAmount;
    private double totalAmount;     // sau giảm giá
    private OrderStatus status;     // COMPLETED, CANCELLED
    private String notes;

    // Transient
    @Transient private List<OrderItem> items;
    @Transient private String customerName;
}

public enum OrderStatus { COMPLETED, CANCELLED }
```

### OrderItem.java
```java
@Data @Builder
public class OrderItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private String productName;     // snapshot tên lúc bán
    private double unitPrice;       // snapshot giá lúc bán
    private double costPrice;       // snapshot giá vốn
    private double quantity;
    private double discountAmount;
    private double lineTotal;       // (unitPrice * quantity) - discountAmount
}
```

---

## Order Code Generation

```java
// Format: ORD-YYYYMMDD-NNNN
// Example: ORD-20240115-0001

private String generateOrderCode() {
    String date = DateUtils.format(LocalDate.now(), "yyyyMMdd");
    // Query: SELECT COUNT(*) FROM orders WHERE DATE(created_at) = TODAY
    int todayCount = orderRepo.countToday() + 1;
    return String.format("ORD-%s-%04d", date, todayCount);
}
```

---

## OrderServiceImpl.createOrder() — QUAN TRỌNG NHẤT

```java
@Override
public Order createOrder(Cart cart, Long customerId, double discountAmount) {

    // === VALIDATION ===
    if (cart == null || cart.getItems().isEmpty()) {
        throw new ValidationException("Giỏ hàng trống");
    }
    if (discountAmount < 0) {
        throw new ValidationException("Giảm giá không hợp lệ");
    }

    // === CHẠY TRONG TRANSACTION ===
    return dbManager.executeInTransaction(() -> {

        // 1. Tạo Order header
        double subtotal = cart.getSubtotal();
        double total = subtotal - discountAmount;
        if (total < 0) total = 0;

        Order order = Order.builder()
            .orderCode(generateOrderCode())
            .customerId(customerId)
            .subtotal(subtotal)
            .discountAmount(discountAmount)
            .totalAmount(total)
            .status(OrderStatus.COMPLETED)
            .build();

        order = orderRepo.save(order);  // lấy ID

        // 2. Tạo OrderItems (snapshot sản phẩm)
        List<OrderItem> savedItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            // Kiểm tra tồn kho một lần nữa (race condition protection)
            Product product = productService.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại"));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                    "Không đủ hàng: " + product.getName() +
                    " (cần " + cartItem.getQuantity() + ", còn " + product.getStockQuantity() + ")");
            }

            OrderItem item = OrderItem.builder()
                .orderId(order.getId())
                .productId(product.getId())
                .productName(product.getName())    // snapshot
                .unitPrice(cartItem.getUnitPrice())
                .costPrice(product.getCostPrice()) // snapshot
                .quantity(cartItem.getQuantity())
                .discountAmount(cartItem.getItemDiscount())
                .lineTotal(cartItem.getLineTotal())
                .build();

            savedItems.add(orderItemRepo.save(item));

            // 3. Trừ tồn kho
            productService.updateStock(product.getId(), -cartItem.getQuantity());
        }

        order.setItems(savedItems);

        // 4. Audit log
        auditLog("CREATE_ORDER", "ORDER", order.getId(),
            "Tổng: " + CurrencyUtils.formatVND(total));

        // 5. Publish event
        EventBus.publish(new OrderCompletedEvent(order.getId(), savedItems));

        return order;

        // NẾU BẤT KỲ BƯỚC NÀO throw exception → ROLLBACK TỰ ĐỘNG
    });
}
```

---

## PaymentServiceImpl.processPayment()

```java
@Override
public Payment processPayment(Long orderId, double amountPaid, Long customerId) {

    Order order = orderService.findById(orderId)
        .orElseThrow(() -> new NotFoundException("Đơn hàng không tồn tại: " + orderId));

    double total = order.getTotalAmount();
    boolean isDebt = amountPaid < total;
    double change = isDebt ? 0 : (amountPaid - total);
    double debtAmount = isDebt ? (total - amountPaid) : 0;

    Payment payment = Payment.builder()
        .orderId(orderId)
        .method(isDebt ? PaymentMethod.DEBT : PaymentMethod.CASH)
        .amountPaid(amountPaid)
        .changeAmount(change)
        .isDebt(isDebt)
        .build();

    paymentRepo.save(payment);

    // Ghi nợ nếu cần
    if (isDebt && customerId != null) {
        debtService.recordDebt(customerId, orderId, debtAmount,
            "Mua thiếu - " + order.getOrderCode());
    }

    return payment;
}

@Override
public double calculateChange(double totalAmount, double amountPaid) {
    double change = amountPaid - totalAmount;
    return Math.max(0, change);
}
```

---

## Checklist Hoàn Thành

- [ ] `createOrder()` chạy trong transaction — rollback khi lỗi
- [ ] Snapshot tên + giá sản phẩm vào order_items
- [ ] Trừ stock đúng sau tạo đơn
- [ ] `cancelOrder()` hoàn trả stock và đổi status = CANCELLED
- [ ] `processPayment()` ghi nợ đúng khi thiếu tiền
- [ ] `calculateChange()` không trả giá trị âm
- [ ] Order code không trùng (ngay cả khi tạo nhanh)
- [ ] Unit tests đầy đủ các trường hợp edge case
