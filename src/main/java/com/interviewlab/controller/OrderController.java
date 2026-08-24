package com.interviewlab.controller;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.dto.PatchOrderRequest;
import com.interviewlab.dto.UpdateOrderRequest;
import com.interviewlab.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

//    @Autowired // we will use construtor injection instead of field autowiring
    private final OrderService orderService;

//    public OrderController(OrderService orderService)
//    {
//        this.orderService=orderService;
//    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse>  createOrder(
            @RequestBody // missed it
                    @Valid
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

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders()
    {
//        List<OrderResponse> allOrders = orderService.getAllOrders();
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable("id")Long id,
    @Valid @RequestBody UpdateOrderRequest updateOrderRequest) // here I missed @Valid
    {
        return ResponseEntity.ok(orderService.updateOrder(id,updateOrderRequest));
    }

    @PatchMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> patchOrder(

            @PathVariable("id") Long id,
            @RequestBody @Valid PatchOrderRequest patchOrderRequest
            )
    {
        return ResponseEntity.ok(orderService.patchOrder(id,patchOrderRequest));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder( @PathVariable("id") Long id)
    {
//        return ResponseEntity.ok(orderService.deleteOrder(id)); // I did this
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build(); // 204 status
    }

    // test the customer order jpa relationship working
    @PostMapping("/test/customer-order")
    public ResponseEntity<String> testCustomerOrder(){
        orderService.testCustomerOrderRelationship();

        return ResponseEntity.ok("Customer + Order created");
    }

    @PostMapping("/test/dirty-checking/{id}")
    public ResponseEntity<String> testDirtyChecking(@PathVariable Long id)
    {
        orderService.testDirtyChecking(id);
        return ResponseEntity.ok("Dirty checking test completed");
    }
    @PostMapping("/test/detach-checking/{id}")
    public ResponseEntity<String> testDetachedEntity(@PathVariable Long id)
    {
        orderService.testDetachedEntity(id);
        return ResponseEntity.ok("Dirty checking test completed");
    }

    @PostMapping("/test/merged-checking/{id}")
    public ResponseEntity<String> testMergeOrder(@PathVariable Long id)
    {
        orderService.testMerge(id);
        return ResponseEntity.ok("Merge checking test completed");
    }
}
