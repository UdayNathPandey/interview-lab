package com.interviewlab.entity;

import java.time.LocalDateTime;

public class Order {
    private Long id;
    private String customerName;
    private String customerEmail;
    private Double amount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
