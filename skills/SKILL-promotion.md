# SKILL — module-promotion

## Vai Trò Của Bạn
Bạn implement `module-promotion` — quản lý khuyến mãi và engine tính giảm giá.

## Điều Kiện Tiên Quyết
`module-core` ✅

## Đọc Bắt Buộc
- `docs/02-DATABASE-SCHEMA.md` — bảng: `promotions`, `promotion_rules`
- `docs/03-API-CONTRACTS.md` — mục PromotionService, DiscountEngine, DiscountResult
- `docs/04-CODING-STANDARDS.md`

---

## DiscountEngine — Logic Chi Tiết

```java
public class DiscountEngineImpl implements DiscountEngine {

    @Override
    public DiscountResult calculate(Cart cart, Customer customer) {
        List<Promotion> activePromos = promotionService.findActive();
        double bestDiscount = 0;
        List<String> appliedNames = new ArrayList<>();

        for (Promotion promo : activePromos) {
            double discount = calculatePromotion(promo, cart);
            if (discount > bestDiscount) {
                bestDiscount = discount;
                appliedNames = List.of(promo.getName());
            }
        }

        // Áp dụng chiết khấu khách quen (stack riêng)
        double customerDiscount = 0;
        if (customer != null && customer.getDiscountRate() > 0) {
            customerDiscount = cart.getSubtotal() * customer.getDiscountRate();
            appliedNames.add("Khách quen: " + (int)(customer.getDiscountRate()*100) + "%");
        }

        double total = bestDiscount + customerDiscount;
        return new DiscountResult(total, appliedNames);
    }

    private double calculatePromotion(Promotion promo, Cart cart) {
        if (!isActive(promo)) return 0;
        if (cart.getSubtotal() < promo.getMinOrderValue()) return 0;

        return switch (promo.getType()) {
            case PERCENT -> cart.getSubtotal() * (promo.getValue() / 100.0);
            case FIXED   -> Math.min(promo.getValue(), cart.getSubtotal());
            case BUY_X_GET_Y -> calculateBuyXGetY(promo, cart);
            case COMBO   -> calculateCombo(promo, cart);
        };
    }

    private boolean isActive(Promotion promo) {
        LocalDate today = LocalDate.now();
        if (promo.getStartDate() != null && today.isBefore(promo.getStartDate())) return false;
        if (promo.getEndDate() != null && today.isAfter(promo.getEndDate())) return false;
        return promo.isActive();
    }
}
```

---

## Checklist Hoàn Thành
- [ ] CRUD Promotion hoạt động
- [ ] `findActive()` lọc đúng theo ngày + is_active
- [ ] PERCENT discount tính đúng
- [ ] FIXED discount không vượt quá tổng đơn
- [ ] BUY_X_GET_Y hoạt động đúng với nhiều item
- [ ] Customer discount cộng thêm vào (không thay thế promo discount)
- [ ] `min_order_value` được kiểm tra trước khi áp dụng
- [ ] Unit tests cho tất cả loại khuyến mãi
