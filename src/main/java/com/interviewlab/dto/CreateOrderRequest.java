package com.interviewlab.dto;

import jakarta.validation.constraints.*;
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
    @NotNull
    @NotBlank
    @Size(min=3,max=50)
    private String customerName;

    @NotNull
    @Email
    private String customerEmail;

    @NotNull
    @Positive
    @Max(1000000)
    private BigDecimal amount; // updated the DT from double to BigDecimal
}
