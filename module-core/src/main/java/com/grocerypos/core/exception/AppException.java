package com.grocerypos.core.exception;

/**
 * Lớp Exception cơ sở cho toàn bộ ứng dụng.
 */
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
