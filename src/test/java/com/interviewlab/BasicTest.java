package com.interviewlab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;

public class BasicTest {

    @Test
    void additionShouldWork()
    {
        int result = 2+3;
        Assertions.assertEquals(5,result);
    }

    @Test
    void discountShouldBeLessThanAmount()
    {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal discount = new BigDecimal("900.00");

        Assertions.assertTrue(discount.compareTo(amount)<0);
    }
}
