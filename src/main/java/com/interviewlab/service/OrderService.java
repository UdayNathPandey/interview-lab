package com.interviewlab.service;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.dto.UpdateOrderRequest;
import com.interviewlab.entity.Order;
import java.util.List;

public interface OrderService {
    public OrderResponse createOrder(CreateOrderRequest orderRequestDto);
    public OrderResponse getOrderById(Long id);
    public List<OrderResponse> getAllOrders();
    public OrderResponse updateOrder(Long id ,UpdateOrderRequest updateOrderRequest);
    public Order deleteOrder();

}
