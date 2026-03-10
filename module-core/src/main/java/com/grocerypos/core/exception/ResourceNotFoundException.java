package com.grocerypos.core.exception;

/**
 * Exception ném ra khi không tìm thấy tài nguyên (Sản phẩm, Khách hàng, v.v.).
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
