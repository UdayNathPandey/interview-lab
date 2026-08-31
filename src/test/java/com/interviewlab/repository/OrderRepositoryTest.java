package com.interviewlab.repository;

import com.interviewlab.entity.mysql.Order;
import com.interviewlab.entity.mysql.OrderStatus;
import com.interviewlab.repository.mysql.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    OrderRepository orderRepository;

    @Test
    void shouldFindOrdersByCustomerName() {

        Order order = Order.builder()
                .customerName("Uday123")
                .customerEmail("uday@gmail.com")
                .amount(new BigDecimal("1000.00"))
                .discount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        List<Order> result =
                orderRepository.findByCustomerName("Uday123");

        Assertions.assertEquals(1, result.size());

        Assertions.assertEquals(
                "Uday123",
                result.get(0).getCustomerName());
    }

    @Test
    void shouldSaveOrder() {

        Order order = Order.builder()
                .customerName("RollbackUser")
                .customerEmail("rollback@gmail.com")
                .amount(new BigDecimal("1000.00"))
                .discount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        orderRepository.flush();

        Assertions.assertNotNull(order.getId());
    }
    @Test
    void shouldStartWithCleanDatabase() {

        List<Order> orders =
                orderRepository.findAll();

        Assertions.assertTrue(orders.isEmpty());
    }
    @Test
    void shouldRollbackAfterTest() {

        Order order = Order.builder()
                .customerName("RollbackUser")
                .customerEmail("rollback@gmail.com")
                .amount(new BigDecimal("1000.00"))
                .discount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        orderRepository.flush();

        List<Order> result =
                orderRepository.findByCustomerName("RollbackUser");

        Assertions.assertEquals(1, result.size());

        System.out.println("Inside test = " + result.size());
    }
}