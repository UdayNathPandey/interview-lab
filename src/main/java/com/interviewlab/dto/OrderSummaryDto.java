package com.interviewlab.dto;

import lombok.*;

import java.math.BigDecimal;

//@AllArgsConstructor //DTO projection dekhne k liye manual constructor rkhte h abhi
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderSummaryDto {

    private Long orderId;
    private BigDecimal amount;
    private String customerName;
    private String customerEmail;

    // constructor
    public OrderSummaryDto(
            Long orderId,
            BigDecimal amount,
            String customerName,
            String customerEmail
    ) {
        this.orderId = orderId;
        this.amount = amount;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }
    // getters
}