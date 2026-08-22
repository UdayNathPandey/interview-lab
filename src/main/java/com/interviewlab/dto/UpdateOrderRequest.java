package com.interviewlab.dto;

import com.interviewlab.entity.OrderStatus;
import com.interviewlab.validation.ValidOrderDiscount;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter


public class UpdateOrderRequest {
    @NotBlank
    @Size(min=3,max=50)
    private String customerName;

    @NotNull
    @Email
    private String customerEmail;

    @NotNull
    @DecimalMin(value="0.01")
    @DecimalMax(value="1000000.00")
    private BigDecimal amount; // updated the DT from double to BigDecimal

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal discount;

    @NotNull
    private OrderStatus status;
}
