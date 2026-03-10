# SKILL — module-customer

## Vai Trò Của Bạn
Bạn implement `module-customer` — quản lý khách hàng thân thiết và hệ thống công nợ.

## Điều Kiện Tiên Quyết
`module-core` ✅

## Đọc Bắt Buộc
- `docs/02-DATABASE-SCHEMA.md` — bảng: `customers`, `debt_records`
- `docs/03-API-CONTRACTS.md` — mục CustomerService, DebtService
- `docs/04-CODING-STANDARDS.md`

---

## Entity Spec

### Customer.java
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Customer extends BaseEntity {
    private String name;          // NOT NULL
    private String phone;         // UNIQUE
    private String address;
    private double discountRate;  // 0.0 - 1.0 (0% - 100%)
    private double totalDebt;     // >= 0, tổng nợ hiện tại
    private String notes;
    private boolean active;
}
```

### DebtRecord.java
```java
@Data @Builder
public class DebtRecord extends BaseEntity {
    private Long customerId;
    private Long orderId;         // null nếu trả nợ thủ công
    private DebtType type;        // BORROW | REPAY
    private double amount;        // luôn dương
    private String note;
}
public enum DebtType { BORROW, REPAY }
```

---

## Logic Công Nợ — BẮT BUỘC ATOMIC

```java
// Ghi nợ — PHẢI trong transaction
public DebtRecord recordDebt(Long customerId, Long orderId, double amount, String note) {
    return dbManager.executeInTransaction(() -> {
        // 1. Insert debt_record (type = BORROW)
        DebtRecord record = DebtRecord.builder()
            .customerId(customerId).orderId(orderId)
            .type(DebtType.BORROW).amount(amount).note(note)
            .build();
        debtRepo.save(record);

        // 2. Cộng vào customers.total_debt (atomic SQL update)
        // SQL: UPDATE customers SET total_debt = total_debt + ? WHERE id = ?
        customerRepo.addDebt(customerId, amount);

        return record;
    });
}

// Trả nợ — PHẢI trong transaction
public DebtRecord recordRepayment(Long customerId, double amount, String note) {
    Customer customer = customerRepo.findById(customerId)
        .orElseThrow(() -> new NotFoundException("Khách hàng không tồn tại"));

    // KHÔNG cho trả quá số nợ hiện tại
    double actualAmount = Math.min(amount, customer.getTotalDebt());
    if (actualAmount <= 0) {
        throw new ValidationException("Khách hàng không có nợ cần trả");
    }

    return dbManager.executeInTransaction(() -> {
        DebtRecord record = DebtRecord.builder()
            .customerId(customerId).type(DebtType.REPAY)
            .amount(actualAmount).note(note).build();
        debtRepo.save(record);

        // SQL: UPDATE customers SET total_debt = total_debt - ? WHERE id = ?
        customerRepo.subtractDebt(customerId, actualAmount);
        return record;
    });
}
```

---

## Checklist Hoàn Thành
- [ ] CRUD Customer đầy đủ
- [ ] `findByPhone()` hoạt động (dùng nhiều ở POS khi tìm khách)
- [ ] `recordDebt()` và `recordRepayment()` chạy atomic
- [ ] `total_debt` không bao giờ < 0
- [ ] `getHistory()` sort theo created_at DESC
- [ ] Unit tests cho debt logic
