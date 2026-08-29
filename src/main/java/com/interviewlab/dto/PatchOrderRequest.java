package com.interviewlab.dto;

import com.interviewlab.entity.mysql.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchOrderRequest {

    @Size(min=3,max=50)
    private String customerName;


    @Email
    private String customerEmail;


    @DecimalMin(value="0.01")
    @DecimalMax(value="1000000.00")
    private BigDecimal amount;


    @DecimalMin("0.00")
    private BigDecimal discount;


    private OrderStatus status;

}
