package com.grocerypos.core.exception;

/**
 * Exception ném ra khi dữ liệu đầu vào không hợp lệ.
 */
public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message);
    }
}
