package com.grocerypos.core.event.events;

import com.grocerypos.core.event.AppEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Sự kiện khi một đơn hàng hoàn thành.
 */
@Getter
@AllArgsConstructor
public class OrderCompletedEvent implements AppEvent {
    private final Long orderId;
    private final double totalAmount;
}
