package com.interviewlab.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=OrderDiscountValidator.class)
@Documented // ye kyo use hota h?
public @interface ValidOrderDiscount {
    String message() default "Discount must be less than Amount !!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[]payload() default {};
}
