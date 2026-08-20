package com.interviewlab.service;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.entity.OrderStatus;
import com.interviewlab.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.interviewlab.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImp implements OrderService{

    @Autowired
    OrderRepository orderRepository;

    public OrderResponse createOrder(CreateOrderRequest orderRequestDto) {

        // name, email and amount
        Order order = Order
                .builder()
                .customerName(orderRequestDto.getCustomerName())
                .customerEmail(orderRequestDto.getCustomerEmail())
                .amount(orderRequestDto.getAmount())
                .status(OrderStatus.PENDING) // missed it
                .createdAt(LocalDateTime.now()) // missed it
                .updatedAt(LocalDateTime.now()) // missed it
                .build();
        Order orderCreated = orderRepository.save(order);

        return OrderResponse.builder()
                .id(orderCreated.getId())
                .customerName(orderCreated.getCustomerName())
                .customerEmail(orderCreated.getCustomerEmail())
                .amount(orderCreated.getAmount())
                .status(orderCreated.getStatus())
                .createdAt(orderCreated.getCreatedAt())
                .updatedAt(orderCreated.getUpdatedAt())
                .build();
    }

    public Order getOrderById(){return null;}

    public List<Order> getAllOrders(){return null;}

    public Order updateOrder(){return null;}

    public Order deleteOrder(){return null;}



}
