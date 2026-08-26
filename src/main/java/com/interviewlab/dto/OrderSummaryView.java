package com.interviewlab.dto;

import java.math.BigDecimal;

public interface OrderSummaryView {

    Long getOrderId();

    BigDecimal getAmount();

//    String getCustomerName();
//
//    String getCustomerEmail();

    // ab nested projection dekhne k liye upr wale ko comment kra

    CustomerView getCustomer();
}