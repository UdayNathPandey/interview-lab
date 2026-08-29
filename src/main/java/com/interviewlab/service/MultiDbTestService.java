package com.interviewlab.service;

import com.interviewlab.entity.h2.AuditLog;
import com.interviewlab.entity.mysql.Order;
import com.interviewlab.entity.mysql.OrderStatus;
import com.interviewlab.repository.h2.AuditLogRepository;
import com.interviewlab.repository.mysql.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MultiDbTestService {

    private final AuditLogRepository auditLogRepository;
    private final OrderRepository orderRepository;

//    @Transactional // agr name na do to data insert ho jayega in h2 even after rollback
    @Transactional("h2TransactionManager")
    public void saveAudit() {

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CREATE_ORDER after transaction");
        auditLog.setMessage("Order created after transaction successfully");

        auditLogRepository.save(auditLog);
        throw new RuntimeException("Testing Rollback");
    }

    @Transactional
    public void saveOrder() {

        Order order = Order.builder()
                .customerName("Multi DB Customer after transaction")
                .customerEmail("multidbaftertransaction@gmail.com")
                .amount(new BigDecimal("5000.00"))
                .discount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        throw new RuntimeException("testing Rollback");
    }

    @Transactional
    public void testTwoDatabases() {

        Order order = new Order();
        // set values

        orderRepository.save(order);

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("TEST");
        auditLog.setMessage("Two DB test");

        auditLogRepository.save(auditLog);

        throw new RuntimeException("FAIL");
    }
}