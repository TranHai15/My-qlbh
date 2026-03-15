package com.grocerypos.customer.event;

import com.google.common.eventbus.Subscribe;
import com.grocerypos.core.event.events.OrderCompletedEvent;
import com.grocerypos.customer.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class OrderEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private final CustomerService customerService;

    public OrderEventListener(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Subscribe
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("[DEBUG] [EVENT-POINTS] Nhận sự kiện tích điểm cho Đơn: {}, Khách: {}", event.getOrderId(), event.getCustomerId());
        
        if (event.getCustomerId() == null) {
            log.info("[DEBUG] [EVENT-POINTS] Khách vãng lai, bỏ qua tích điểm.");
            return;
        }

        log.info("[DEBUG] [EVENT-POINTS] Đang khởi chạy luồng ASYNC để tích điểm...");
        CompletableFuture.runAsync(() -> {
            try {
                log.info("[DEBUG] [EVENT-POINTS-ASYNC] Luồng phụ bắt đầu xử lý tích điểm cho Khách ID: {}", event.getCustomerId());
                
                double earnedAmount = event.getTotalAmount() * 0.01;
                double points = Math.floor(earnedAmount / 1000); 
                
                if (points > 0) {
                    customerService.updatePoints(event.getCustomerId(), points);
                    log.info("[DEBUG] [EVENT-POINTS-ASYNC] TÍCH ĐIỂM THÀNH CÔNG: +{} điểm.", points);
                } else {
                    log.info("[DEBUG] [EVENT-POINTS-ASYNC] Giá trị tích lũy ({}) không đủ 1 điểm (1000đ), bỏ qua.", earnedAmount);
                }
            } catch (Exception e) {
                log.error("[DEBUG] [EVENT-POINTS-ASYNC] LỖI khi tích điểm ngầm!", e);
            }
        });
        log.info("[DEBUG] [EVENT-POINTS] Phương thức handleOrderCompleted đã kết thúc (luồng phụ vẫn đang chạy).");
    }
}
