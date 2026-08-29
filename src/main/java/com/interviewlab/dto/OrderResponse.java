package com.interviewlab.dto;

import com.interviewlab.entity.mysql.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor // missed it
@AllArgsConstructor// missed it
@Builder
@Getter// missed it - required by jackson
public class OrderResponse {
    private Long id;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal discount;
}
