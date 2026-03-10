package com.grocerypos.core.event;

import com.google.common.eventbus.EventBus;

/**
 * Wrapper cho Guava EventBus để giao tiếp lỏng lẻo (decoupling) giữa các module.
 */
public class AppEventBus {
    private static final EventBus EVENT_BUS = new EventBus("GroceryPOS-EventBus");

    public static void post(Object event) {
        EVENT_BUS.post(event);
    }

    public static void register(Object listener) {
        EVENT_BUS.register(listener);
    }

    public static void unregister(Object listener) {
        EVENT_BUS.unregister(listener);
    }
}
