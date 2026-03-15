package com.grocerypos.customer.service;

import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.customer.entity.Customer;
import com.grocerypos.customer.repository.CustomerRepository;
import com.grocerypos.customer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepo;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepo);
    }

    @Test
    void save_ValidData_Success() {
        Customer c = Customer.builder().name("John Doe").phone("0987654321").discountRate(0.1).build();
        when(customerRepo.findByPhone("0987654321")).thenReturn(Optional.empty());
        when(customerRepo.save(any())).thenReturn(c);

        Customer created = customerService.save(c);

        assertNotNull(created);
        assertEquals("John Doe", created.getName());
        verify(customerRepo).save(any());
    }

    @Test
    void save_DuplicatePhone_ThrowsException() {
        Customer c = Customer.builder().name("John Doe").phone("0987654321").discountRate(0.1).build();
        when(customerRepo.findByPhone("0987654321")).thenReturn(Optional.of(c));

        assertThrows(ValidationException.class, () -> customerService.save(c));
    }

    @Test
    void update_CustomerNotFound_ThrowsException() {
        Customer c = Customer.builder().name("John Doe").phone("0987654321").discountRate(0.1).build();
        c.setId(1L);
        when(customerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> customerService.update(c));
    }
}

