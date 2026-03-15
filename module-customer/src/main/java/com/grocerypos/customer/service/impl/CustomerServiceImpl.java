package com.grocerypos.customer.service.impl;

import com.grocerypos.core.exception.ResourceNotFoundException;
import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.customer.entity.Customer;
import com.grocerypos.customer.repository.CustomerRepository;
import com.grocerypos.customer.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CustomerServiceImpl implements CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private final CustomerRepository customerRepo;

    public CustomerServiceImpl(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    @Override
    public Customer save(Customer customer) {
        validateCustomer(customer);
        if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
            if (customerRepo.findByPhone(customer.getPhone()).isPresent()) {
                throw new ValidationException("Số điện thoại " + customer.getPhone() + " đã tồn tại");
            }
        }
        return customerRepo.save(customer);
    }

    @Override
    public Customer update(Customer customer) {
        if (customer.getId() == null) {
            throw new ValidationException("ID khách hàng không được để trống");
        }
        validateCustomer(customer);
        
        if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
            Optional<Customer> existing = customerRepo.findByPhone(customer.getPhone());
            if (existing.isPresent() && !existing.get().getId().equals(customer.getId())) {
                throw new ValidationException("Số điện thoại này đã được sử dụng bởi khách hàng khác");
            }
        }

        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepo.update(customer);
    }

    @Override
    public void delete(Long id) {
        customerRepo.delete(id);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepo.findById(id);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return customerRepo.findByPhone(phone);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepo.findAll();
    }

    @Override
    public List<Customer> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        return customerRepo.search(keyword.trim());
    }

    @Override
    public List<Customer> findWithDebt() {
        // Chức năng nợ đã bị loại bỏ, trả về danh sách trống hoặc ném lỗi nếu cần
        return List.of();
    }

    @Override
    public double getDiscountRate(Long customerId) {
        return findById(customerId)
                .map(Customer::getDiscountRate)
                .orElse(0.0);
    }

    @Override
    public void updatePoints(Long customerId, double delta) {
        if (delta == 0) return;
        customerRepo.updatePoints(customerId, delta);
        log.info("Đã cập nhật điểm cho khách hàng ID {}: {}", customerId, delta);
    }

    private void validateCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new ValidationException("Tên khách hàng không được để trống");
        }
        if (customer.getDiscountRate() < 0 || customer.getDiscountRate() > 1) {
            throw new ValidationException("Tỉ lệ chiết khấu phải từ 0 đến 100%");
        }
    }
}
