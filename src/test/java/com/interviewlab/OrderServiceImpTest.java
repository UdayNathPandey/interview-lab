package com.interviewlab;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.dto.UpdateOrderRequest;
import com.interviewlab.entity.mysql.Order;
import com.interviewlab.exception.ResourceNotFoundException;
import com.interviewlab.repository.mysql.OrderRepository;
import com.interviewlab.service.OrderServiceImp;
import com.interviewlab.utility.OrderUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class) // to integrate the mockito with junit
public class OrderServiceImpTest {

    @Mock
    OrderRepository orderRepository;

//    @InjectMocks
//    OrderService orderService; // getting MockitoException - Cannot instantiate @InjectMocks field named 'orderService'! Cause: the type 'OrderService' is an interface.

    @InjectMocks
    OrderServiceImp orderServiceImp;
    //@InjectMocks does below conceptually
//    OrderServiceImp service =
//            new OrderServiceImp(orderRepositoryMock);

    @Test
    public void shouldReturnOrderwhenOrderExists()
    {
        Order order = Order.builder()
                .id(1L)
                .customerName("Uday")
                .customerEmail("uday@gmail.com")
                .amount(new BigDecimal("1000.00"))
                .build();

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse orderResponse = orderServiceImp.getOrderById(1L);

        Assertions.assertEquals(1L,orderResponse.getId());
        Assertions.assertEquals(new BigDecimal("1000.00"),
                orderResponse.getAmount());
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist()
    {
        Mockito.when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                ()-> orderServiceImp.getOrderById(999L) // yha lambda hota h ye bhool gya tha mai
        );
    }

    @Test
    void shouldCallRepositoryWithCorrectId()
    {
        Order order = Order.builder()
                .id(1L)
                .amount(new BigDecimal("1000.00"))
                .build();

        Mockito.when(orderRepository.findById(999L))
                .thenReturn(Optional.of(order));

        orderServiceImp.getOrderById(999L);

        Mockito.verify(orderRepository).findById(999L);
    }

    @Test
    void shouldSaveOrder()
    {
        CreateOrderRequest request = CreateOrderRequest
                .builder()
                .customerName("test user")
                .customerEmail("test@gmail.com")
                .amount(new BigDecimal("1000.00"))
                .build();

        Mockito.when(
                orderRepository.save(
                        any(Order.class)
                )
        ).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        OrderResponse orderResponse = orderServiceImp.createOrder(request);

        Mockito.verify(orderRepository).save(any(Order.class));

    }

    @Test
    void shouldSaveCorrectOrder() {

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test User")
                .customerEmail("test@gmail.com")
                .amount(new BigDecimal("1000.00"))
                .build();

        Mockito.when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocationOnMock-> invocationOnMock.getArgument(0));

        orderServiceImp.createOrder(request);

        // ab hme check krna h ki hmne correct order create kra h ya nhi
        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(orderRepository).save(argumentCaptor.capture());

        Order savedOrder = argumentCaptor.getValue();

        Assertions.assertEquals("Test User", savedOrder.getCustomerName());
        Assertions.assertEquals(
                "test@gmail.com",
                savedOrder.getCustomerEmail());

        Assertions.assertEquals(
                new BigDecimal("1000.00"),
                savedOrder.getAmount());


    }

    @Test
    void shouldUpdateOrder() {

        Order existingOrder = Order.builder()
                .id(1L)
                .customerName("Old Name")
                .customerEmail("old@gmail.com")
                .amount(new BigDecimal("1000.00"))
                .build();

        UpdateOrderRequest request =
                UpdateOrderRequest.builder()
                        .customerName("New Name")
                        .customerEmail("new@gmail.com")
                        .amount(new BigDecimal("2000.00"))
                        .build();

        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.of(existingOrder));

        Mockito.when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        orderServiceImp.updateOrder(1L, request);

        ArgumentCaptor<Order> captor =
                ArgumentCaptor.forClass(Order.class);

        Mockito.verify(orderRepository)
                .save(captor.capture());

        Order updatedOrder = captor.getValue();

        Assertions.assertEquals(
                "New Name",
                updatedOrder.getCustomerName());

        Assertions.assertEquals(
                "new@gmail.com",
                updatedOrder.getCustomerEmail());

        Assertions.assertEquals(
                new BigDecimal("2000.00"),
                updatedOrder.getAmount());
    }

    @Test
    void shouldDeleteOrder() {

        Order order = Order.builder()
                .id(1L)
                .build();
        Mockito.when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        orderServiceImp.deleteOrder(1L);

        Mockito.verify(orderRepository).findById(1L);
        Mockito.verify(orderRepository).delete(order);

    }

    @Test
    void shouldNotDeleteWhenOrderDoesNotExist() {

        Mockito.when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                ()-> orderServiceImp.deleteOrder(999L)

        );

        Mockito.verify(
                orderRepository,
                        Mockito.times(1)
                )
                .findById(999L);

        Mockito.verify(
                orderRepository,
                        Mockito.never()
                )
                .delete(any(Order.class));

    }

    @Test
    void shouldCalculateFinalAmountThroughCreateOrder() {

        CreateOrderRequest request =
                CreateOrderRequest.builder()
                        .customerName("Test User")
                        .customerEmail("test@gmail.com")
                        .amount(new BigDecimal("1000.00"))
                        .discount(new BigDecimal("100.00"))
                        .build();

        Mockito.when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        OrderResponse orderResponse = orderServiceImp.createOrder(request);

        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(orderRepository, Mockito.times(1))
                .save(argumentCaptor.capture());

        Order savedOrder = argumentCaptor.getValue();

        Assertions.assertEquals(
                new BigDecimal("900.00"),
                savedOrder.getAmount());



    }

    // testing static method
    @Test
    void shouldUseMockedStaticMethod(){
        try(
                MockedStatic<OrderUtils> mockedStatic = Mockito.mockStatic(OrderUtils.class)
                )
        {
            mockedStatic.when(()->OrderUtils.generateOrderReference(1L))
                    .thenReturn("MOCK-001");

            String result = orderServiceImp.generateReference(1L);

            Assertions.assertEquals("MOCK-001",result);

//            Mockito.verify(OrderUtils, Mockito.times(1))
//                    .generateOrderReference(1L);

            mockedStatic.verify(
                    ()-> OrderUtils.generateOrderReference(1L)
            );
        }
    }
}
