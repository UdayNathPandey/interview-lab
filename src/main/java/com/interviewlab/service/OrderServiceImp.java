package com.interviewlab.service;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.dto.PatchOrderRequest;
import com.interviewlab.dto.UpdateOrderRequest;
import com.interviewlab.entity.OrderStatus;
import com.interviewlab.exception.BadRequestException;
import com.interviewlab.exception.ResourceNotFoundException;
import com.interviewlab.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.interviewlab.entity.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImp implements OrderService{

    @Autowired
    OrderRepository orderRepository;

    @Override
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

    @Override
    public OrderResponse getOrderById(Long id){

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: "+id));
        // orElseThrow lambda leta h ye miss hua
        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Override
    public List<OrderResponse> getAllOrders()
    {
//        List<Order> allOrders = orderRepository.findAll();
//        List<OrderResponse> allOrderResponse=new ArrayList<>();
//        for(Order order : allOrders)
//        {
//            OrderResponse orderResponse= OrderResponse.builder()
//                    .id(order.getId())
//                    .customerName(order.getCustomerName())
//                    .customerEmail(order.getCustomerEmail())
//                    .amount(order.getAmount())
//                    .status(order.getStatus())
//                    .createdAt(order.getCreatedAt())
//                    .updatedAt(order.getUpdatedAt())
//                    .build();
//
//        }
        // yha maine by default for loop se solve kra , should have though about stream while processing collection
        return orderRepository.findAll().stream()
                .map(order ->
                       OrderResponse.builder()
                        .id(order.getId())
                        .customerName(order.getCustomerName())
                        .customerEmail(order.getCustomerEmail())
                        .amount(order.getAmount())
                        .status(order.getStatus())
                        .createdAt(order.getCreatedAt())
                        .updatedAt(order.getUpdatedAt())
                        .build()
                ).toList(); // toList() belongs to 16+
    }

    @Override
    public OrderResponse updateOrder(Long id, UpdateOrderRequest updateOrderRequest)
    {
        //check if requested user exist or not
        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with Id "+id));

        // set the field of the fetched order with updated ones
        order.setCustomerName(updateOrderRequest.getCustomerName());
        order.setCustomerEmail(updateOrderRequest.getCustomerEmail());
        order.setAmount(updateOrderRequest.getAmount());
        order.setDiscount(updateOrderRequest.getDiscount());
        order.setStatus(updateOrderRequest.getStatus());
        order.setUpdatedAt(LocalDateTime.now());
        // save the updated order in database
        Order updatedOrder = orderRepository.save(order);
        //return OrderResponse
        return OrderResponse.builder()
                .id(updatedOrder.getId())
                .customerName(updatedOrder.getCustomerName())
                .customerEmail(updatedOrder.getCustomerEmail())
                .amount(updatedOrder.getAmount())
                .discount(updatedOrder.getDiscount())
                .status(updatedOrder.getStatus())
                .createdAt(updatedOrder.getCreatedAt())
                .updatedAt(updatedOrder.getUpdatedAt())
                .build();
    }

    @Override
    public OrderResponse patchOrder(Long id , PatchOrderRequest patchOrderRequest)
    {
        //check if user exits
        Order fetchedOrder = orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with id "+id));
        //update the user
        if (patchOrderRequest.getCustomerName() != null) {
            fetchedOrder.setCustomerName(patchOrderRequest.getCustomerName());
        }

        if (patchOrderRequest.getCustomerEmail() != null) {
            fetchedOrder.setCustomerEmail(patchOrderRequest.getCustomerEmail());
        }

        if (patchOrderRequest.getAmount() != null) {
            fetchedOrder.setAmount(patchOrderRequest.getAmount());
        }

        if (patchOrderRequest.getDiscount() != null) {
            fetchedOrder.setDiscount(patchOrderRequest.getDiscount());
        }

        if (patchOrderRequest.getStatus() != null) {
            fetchedOrder.setStatus(patchOrderRequest.getStatus());
        }
        fetchedOrder.setUpdatedAt(LocalDateTime.now());
        // save the user
        Order patchedOrder=   orderRepository.save(fetchedOrder);
        // return orderResponse
        return OrderResponse.builder()
                .id(patchedOrder.getId())
                .customerName(patchedOrder.getCustomerName())
                .customerEmail(patchedOrder.getCustomerEmail())
                .amount(patchedOrder.getAmount())
                .discount(patchedOrder.getDiscount())
                .status(patchedOrder.getStatus())
                .createdAt(patchedOrder.getCreatedAt())
                .updatedAt(patchedOrder.getUpdatedAt())
                .build();
    }

    @Override
    public Void deleteOrder(Long id){
        Order fetchOrder = orderRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Order not found with id "+id));
        orderRepository.delete(fetchOrder);

        return null; // Void k liye null return krte h ye nhi pta tha
    }



}
