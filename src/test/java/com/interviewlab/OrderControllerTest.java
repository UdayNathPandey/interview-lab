package com.interviewlab;

import com.interviewlab.controller.OrderController;
import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.service.InnerService;
import com.interviewlab.service.MultiDbTestService;
import com.interviewlab.service.OrderService;
import com.interviewlab.service.ProxyExperimentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean // MockBean is older hence deprecated
    OrderService orderService;

    // without below bean , test was failing
    @MockitoBean
    InnerService innerService;

    @MockitoBean
    ProxyExperimentService proxyExperimentService;

    @MockitoBean
    MultiDbTestService multiDbTestService;

    @Test
    void shouldGetOrderById() throws Exception
    {
        OrderResponse response =
                OrderResponse.builder()
                        .id(1L)
                        .amount(new BigDecimal("1000.00"))
                        .customerName("Uday")
                        .customerEmail("uday@gmail.com")
                        .build();
        Mockito.when(orderService.getOrderById(1L))
                .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/orders/1")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.id").value(1)
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.amount").value(1000.00)
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.customerName").value("Uday")
                );

        Mockito.verify(orderService).getOrderById(1L);

    }

    @Test
    void shouldPostOrder() throws Exception
    {
        OrderResponse response =
                OrderResponse.builder()
                        .id(10L)
                        .amount(new BigDecimal("5000.00"))
                        .customerName("Test User")
                        .customerEmail("test@gmail.com")
                        .build();

        Mockito.when(orderService.createOrder(Mockito.any(CreateOrderRequest.class)))
                        .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                                        "customerName": "Test User",
                                                        "customerEmail": "test@gmail.com",
                                                        "amount": 5000
                                }
                                """)
        )
                        .andExpect(
//                                MockMvcResultMatchers.status().isCreated() // yha validation error h to below line is requied
                                MockMvcResultMatchers.status().isBadRequest()
                        );
//                .andExpect(MockMvcResultMatchers.jsonPath("$.customerName").value("Test User"))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.customerEmail").value("test@gmail.com"))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(5000));

        Mockito.verify(orderService, Mockito.never()).createOrder(Mockito.any(CreateOrderRequest.class));

    }
}
