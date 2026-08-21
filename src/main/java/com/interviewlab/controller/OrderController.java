package com.interviewlab.controller;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.exception.ResourceNotFoundException;
import com.interviewlab.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("id") Long id)
    {

        // never put try catch in controller -> bad practice -> that is why RestControllerAdvice aya
//        try
//        {
            OrderResponse orderResponse = orderService.getOrderById(id);
//            return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
        return ResponseEntity.ok(orderResponse);
//        }catch (RuntimeException ex) {

//            throw new ResourceNotFoundException("this user is not there in database , bro!!");
//        }
    }
}
