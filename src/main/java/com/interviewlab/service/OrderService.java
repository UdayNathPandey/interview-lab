package com.interviewlab.service;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.dto.PatchOrderRequest;
import com.interviewlab.dto.UpdateOrderRequest;

import java.util.List;

public interface OrderService {
    public OrderResponse createOrder(CreateOrderRequest orderRequestDto);
    public OrderResponse getOrderById(Long id);
    public List<OrderResponse> getAllOrders();
    public OrderResponse updateOrder(Long id ,UpdateOrderRequest updateOrderRequest);
    public OrderResponse patchOrder(Long id, PatchOrderRequest patchOrderRequest);
    public void deleteOrder(Long id);

}
