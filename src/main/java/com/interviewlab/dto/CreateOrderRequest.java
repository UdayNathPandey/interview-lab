package com.interviewlab.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@NoArgsConstructor// missed it - required by jackson
@AllArgsConstructor
@Setter
@Getter
public class CreateOrderRequest {
    private String customerName;
    private String customerEmail;
    private BigDecimal amount; // updated the DT from double to BigDecimal
}
