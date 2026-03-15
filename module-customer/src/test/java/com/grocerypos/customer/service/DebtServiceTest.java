package com.grocerypos.customer.service;

import com.grocerypos.customer.entity.Customer;
import com.grocerypos.customer.entity.DebtRecord;
import com.grocerypos.customer.entity.DebtType;
import com.grocerypos.customer.repository.CustomerRepository;
import com.grocerypos.customer.repository.DebtRepository;
import com.grocerypos.customer.service.impl.DebtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtServiceTest {

    @Mock
    private CustomerRepository customerRepo;

    @Mock
    private DebtRepository debtRepo;

    private DebtService debtService;

    @BeforeEach
    void setUp() {
        debtService = new DebtServiceImpl(customerRepo, debtRepo);
    }

    @Test
    void recordDebt_Success() throws Exception {
        Long customerId = 1L;
        double amount = 50000;
        Customer customer = Customer.builder().name("Test").build();
        customer.setId(customerId);

        when(customerRepo.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepo.runInTransaction(any())).thenAnswer(invocation -> {
            CustomerRepository.TransactionTask<?> task = invocation.getArgument(0);
            return task.execute(mock(Connection.class));
        });

        DebtRecord record = debtService.recordDebt(customerId, null, amount, "Note");

        assertNotNull(record);
        assertEquals(DebtType.BORROW, record.getType());
        assertEquals(amount, record.getAmount());
        
        verify(customerRepo).addDebt(any(), eq(customerId), eq(amount));
        verify(debtRepo).save(any(), any());
    }

    @Test
    void recordRepayment_Success() throws Exception {
        Long customerId = 1L;
        double currentDebt = 100000;
        double repaymentAmount = 40000;
        
        Customer customer = Customer.builder().name("Test").totalDebt(currentDebt).build();
        customer.setId(customerId);

        when(customerRepo.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepo.runInTransaction(any())).thenAnswer(invocation -> {
            CustomerRepository.TransactionTask<?> task = invocation.getArgument(0);
            return task.execute(mock(Connection.class));
        });

        DebtRecord record = debtService.recordRepayment(customerId, repaymentAmount, "Note");

        assertNotNull(record);
        assertEquals(DebtType.REPAY, record.getType());
        assertEquals(repaymentAmount, record.getAmount());
        
        verify(customerRepo).subtractDebt(any(), eq(customerId), eq(repaymentAmount));
        verify(debtRepo).save(any(), any());
    }
}
