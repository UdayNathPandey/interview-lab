package com.interviewlab.utility;

public class OrderUtils {

    public static String generateOrderReference(Long orderId) {
        return "ORD-" + orderId;
    }
}
