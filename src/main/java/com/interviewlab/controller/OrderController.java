package com.interviewlab.controller;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse>  createOrder(
            @RequestBody // missed it
            CreateOrderRequest orderReq)
    {
        OrderResponse orderResponse = orderService.createOrder(orderReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse); // maine phle HttpStatus.OK likha tha
    }

}
