package com.grocerypos.customer.service;

import com.grocerypos.customer.entity.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Interface cho dịch vụ quản lý Khách hàng.
 */
public interface CustomerService {
    // --- CRUD ---
    Customer save(Customer customer);
    Customer update(Customer customer);
    void delete(Long id);

    // --- Query ---
    Optional<Customer> findById(Long id);
    Optional<Customer> findByPhone(String phone);
    List<Customer> findAll();
    List<Customer> search(String keyword);          // tên hoặc SĐT
    List<Customer> findWithDebt();                  // khách có nợ > 0

    // --- Discount ---
    double getDiscountRate(Long customerId);        // trả về 0.0 nếu không có
}
