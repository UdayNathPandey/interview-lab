package com.interviewlab;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.entity.mysql.Order;
import com.interviewlab.exception.ResourceNotFoundException;
import com.interviewlab.repository.mysql.OrderRepository;
import com.interviewlab.service.OrderServiceImp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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


}
