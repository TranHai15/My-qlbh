package com.grocerypos.core.exception;

/**
 * Exception ném ra khi tồn kho không đủ để xuất hàng.
 */
public class InsufficientStockException extends AppException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
