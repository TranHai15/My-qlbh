package com.grocerypos.order.service.impl;

import com.grocerypos.core.exception.ResourceNotFoundException;
import com.grocerypos.customer.repository.CustomerRepository;
import com.grocerypos.order.entity.Order;
import com.grocerypos.order.entity.Payment;
import com.grocerypos.order.repository.PaymentRepository;
import com.grocerypos.order.service.OrderService;
import com.grocerypos.order.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepo;
    private final OrderService orderService;
    private final CustomerRepository customerRepo; // Dùng trực tiếp Repo để cập nhật điểm

    public PaymentServiceImpl(PaymentRepository paymentRepo, OrderService orderService, CustomerRepository customerRepo) {
        this.paymentRepo = paymentRepo;
        this.orderService = orderService;
        this.customerRepo = customerRepo;
    }

    @Override
    public Payment processPayment(Long orderId, double amountPaid, Long customerId) {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng ID: " + orderId));

        double totalAmount = order.getTotalAmount();
        
        // Không cho phép thanh toán thiếu (vì đã bỏ chức năng Nợ)
        if (amountPaid < totalAmount) {
            throw new IllegalArgumentException("Số tiền khách đưa không đủ để thanh toán!");
        }

        double changeAmount = amountPaid - totalAmount;

        Payment payment = Payment.builder()
                .orderId(orderId)
                .method("CASH")
                .amountPaid(amountPaid)
                .changeAmount(changeAmount)
                .isDebt(false)
                .build();

        payment = paymentRepo.save(payment);
        return payment;
    }

    @Override
    public double calculateChange(double totalAmount, double amountPaid) {
        return Math.max(0, amountPaid - totalAmount);
    }
}
