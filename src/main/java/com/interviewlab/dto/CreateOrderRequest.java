package com.interviewlab.dto;

import com.interviewlab.validation.ValidOrderDiscount;
import jakarta.validation.constraints.*; // ye package mai recall nhi kr paya
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@NoArgsConstructor// missed it - required by jackson
@AllArgsConstructor
@Setter
@Getter

@ValidOrderDiscount
public class CreateOrderRequest {
    @NotBlank
    @Size(min=3,max=50)
    private String customerName;

    @NotNull
    @Email
//    @Pattern(
//            regexp="^[a-zA-Z ]+$",
//            message="Name can contain only alphabets and spaces"
//    )
    private String customerEmail;

    @NotNull
//    @PositiveOrZero
    @DecimalMin(value="0.01")
    @DecimalMax(value="1000000.00")
    private BigDecimal amount; // updated the DT from double to BigDecimal

    // business rule : discount < amount
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal discount;
}
