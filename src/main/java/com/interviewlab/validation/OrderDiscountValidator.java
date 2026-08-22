package com.interviewlab.validation;

import com.interviewlab.dto.CreateOrderRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class OrderDiscountValidator implements ConstraintValidator<ValidOrderDiscount, CreateOrderRequest> {

    @Override
    public boolean isValid(
            CreateOrderRequest createOrderRequest,
            ConstraintValidatorContext constraintValidatorContext) {



        if(createOrderRequest==null)return true;

//        if(createOrderRequest.getDiscount().compareTo(createOrderRequest.getAmount())<0)
//            return true;
        BigDecimal amount = createOrderRequest.getAmount();
        BigDecimal discount= createOrderRequest.getDiscount();
        if(amount==null || discount==null)
            return true;

        if(discount.compareTo(amount)<0) return  true;

        // since this is class level validation so fieldError won't be populated and java ko automatically
        // field ka nam hi pata h chalega
        // jo hm manually voilation set kr rhe h isko false se just phle hi rkhna nhi to correct payload me bhi voilation dikhega

        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
                .buildConstraintViolationWithTemplate(
//                        "Discount must be less than amount" // manually dene se better h , annotation ka default message dena
                        constraintValidatorContext.getDefaultConstraintMessageTemplate()
                )
                .addPropertyNode("discount")
                .addConstraintViolation();
        return  false;
    }


}
