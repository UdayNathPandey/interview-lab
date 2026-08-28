package com.interviewlab.service;

import com.interviewlab.dto.*;
import com.interviewlab.entity.Customer;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest orderRequestDto);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrder(Long id ,UpdateOrderRequest updateOrderRequest);
    OrderResponse patchOrder(Long id, PatchOrderRequest patchOrderRequest);
    void deleteOrder(Long id);
    void testCustomerOrderRelationship();
    void testDirtyChecking(Long id);
    void testDetachedEntity(Long id);
    void testMerge(Long id);
    void testCascadePersist();
    void testCascadeMerge(Long id);
    void testCascadeRemove(Long id);
    void testCascadeOrphanRemoval(Long cid,Long oid);
    void testFetchEagerandLazy(Long id);
    void testCustomerFetch(Long id);
    void testNPlusOne();
    void testJoinFetch();
    List<OrderSummaryDto> getOrderSummaries();
    List<OrderSummaryView> getOrderSummaryViews();
    void testTransactionSuccess();
    void testTransactionRollback();
    void testCheckedExceptionRollback() throws Exception;
    void testRollbackFor() throws Exception;
    void testNoRollbackFor();
    void testRequired();
    void createOrderInNewTransaction();
    void supportsMethod();


}
