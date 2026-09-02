package com.interviewlab.controller;

import com.interviewlab.dto.*;
import com.interviewlab.entity.mysql.Order;
import com.interviewlab.entity.mysql.OrderStatus;
import com.interviewlab.service.InnerService;
import com.interviewlab.service.MultiDbTestService;
import com.interviewlab.service.OrderService;
import com.interviewlab.service.ProxyExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    //    @Autowired // we will use construtor injection instead of field autowiring
    private final OrderService orderService;
    private final InnerService innerService;

//    public OrderController(OrderService orderService)
//    {
//        this.orderService=orderService;
//    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody // missed it
            @Valid
            CreateOrderRequest orderReq) {
        OrderResponse orderResponse = orderService.createOrder(orderReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse); // maine phle HttpStatus.OK likha tha
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("id") Long id) {

        // never put try catch in controller -> bad practice -> that is why RestControllerAdvice aya
//        try
//        {
        OrderResponse orderResponse = orderService.getOrderById(id);
//            return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
        return ResponseEntity.ok(orderResponse);
//        }catch (RuntimeException ex) {

//            throw new ResourceNotFoundException("this user is not there in database , bro!!");
//        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required=false) OrderStatus orderStatus
            ,Pageable pageable) {
//        List<OrderResponse> allOrders = orderService.getAllOrders();
        return ResponseEntity.ok(orderService.getAllOrders(orderStatus,pageable));
    }

//    // testing pagination, sorting and filtering
//    @GetMapping("/orders")
//    public ResponseEntity<Page<Order>> getAllOrders(Pageable pageable) {
////        List<OrderResponse> allOrders = orderService.getAllOrders();
//        return ResponseEntity.ok(orderService.getAllOrders(pageable));
//    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateOrderRequest updateOrderRequest) // here I missed @Valid
    {
        return ResponseEntity.ok(orderService.updateOrder(id, updateOrderRequest));
    }

    @PatchMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> patchOrder(

            @PathVariable("id") Long id,
            @RequestBody @Valid PatchOrderRequest patchOrderRequest
    ) {
        return ResponseEntity.ok(orderService.patchOrder(id, patchOrderRequest));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
//        return ResponseEntity.ok(orderService.deleteOrder(id)); // I did this
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build(); // 204 status
    }

    // test the customer order jpa relationship working
    @PostMapping("/test/customer-order")
    public ResponseEntity<String> testCustomerOrder() {
        orderService.testCustomerOrderRelationship();

        return ResponseEntity.ok("Customer + Order created");
    }

    @PostMapping("/test/dirty-checking/{id}")
    public ResponseEntity<String> testDirtyChecking(@PathVariable Long id) {
        orderService.testDirtyChecking(id);
        return ResponseEntity.ok("Dirty checking test completed");
    }

    @PostMapping("/test/detach-checking/{id}")
    public ResponseEntity<String> testDetachedEntity(@PathVariable Long id) {
        orderService.testDetachedEntity(id);
        return ResponseEntity.ok("Dirty checking test completed");
    }

    @PostMapping("/test/merged-checking/{id}")
    public ResponseEntity<String> testMergeOrder(@PathVariable Long id) {
        orderService.testMerge(id);
        return ResponseEntity.ok("Merge checking test completed");
    }

    @PostMapping("/test/cascade-persist")
    public ResponseEntity<String> testCascadePersist() {
        orderService.testCascadePersist();
        return ResponseEntity.ok("Cascade persist test completed");
    }

    @PostMapping("/test/cascade-merge/{id}")
    public ResponseEntity<String> testMergeCascade(@PathVariable Long id) {
        orderService.testCascadeMerge(id);
        return ResponseEntity.ok("Merge checking test completed");
    }

    @DeleteMapping("/test/cascade-remove/{id}")
    public ResponseEntity<Void> testRemoveCascade(@PathVariable(name = "id") Long id) {
        orderService.testCascadeRemove(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/test/cascade-orphanRemoval/{customerId}/{orderId}")
    public ResponseEntity<Void> testCascadeOrphanRemoval(
            @PathVariable("customerId") Long cid,
            @PathVariable("orderId") Long oid
    ) {
        orderService.testCascadeOrphanRemoval(cid, oid);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/test/fetchEagerLazyOrder/{id}")
    public ResponseEntity<String> testFetchEagerOrder(@PathVariable("id") Long id) {
        orderService.testFetchEagerandLazy(id);
        return ResponseEntity.ok("testing Fetch Eager completed");

    }

    @GetMapping("/test/fetchEagerLazyCustomer/{id}")
    public ResponseEntity<String> testFetchEagerCustomer(@PathVariable("id") Long id) {
        orderService.testCustomerFetch(id);
        return ResponseEntity.ok("testing Fetch Eager completed");

    }

    @GetMapping("/test/n-plus-one")
    public ResponseEntity<Void> testNPlusOne() {

        orderService.testNPlusOne();
        return ResponseEntity.ok().build();

    }

    @GetMapping("/test/joinfetch")
    public ResponseEntity<Void> testJoinFetch() {

        orderService.testJoinFetch();
        return ResponseEntity.ok().build();

    }

    @GetMapping("/orders/summary")
    public ResponseEntity<List<OrderSummaryDto>> getOrderSummaries()
    {
        return ResponseEntity.ok(orderService.getOrderSummaries());
    }

    @GetMapping("/orders/summary-interface")
    public ResponseEntity<List<OrderSummaryView>> getOrderSummaryViews() {

        return ResponseEntity.ok(
                orderService.getOrderSummaryViews()
        );
    }

    @PostMapping("/test/transaction-success")
    public ResponseEntity<Void> testTransactionSuccess() {

        orderService.testTransactionSuccess();

        return ResponseEntity.ok().build();
    }
    @PostMapping("/test/transaction-failure")
    public ResponseEntity<Void> testTransactionFailure() {

        orderService.testTransactionRollback();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/checked-exception")
    public ResponseEntity<Void> testCheckedException()
            throws Exception {

        orderService.testCheckedExceptionRollback();

        return ResponseEntity.ok().build();
    }
    @PostMapping("/test/checked-exception-with-rollbackFor")
    public ResponseEntity<Void> testCheckedExceptionWithRollbackFor()
            throws Exception {

        orderService.testRollbackFor();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/unchecked-exception-with-norollbackFor")
    public ResponseEntity<Void> testUnCheckedExceptionWithNoRollbackFor()
            throws Exception {

        orderService.testNoRollbackFor();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/required-propagation")
    public ResponseEntity<Void> testRequiredPropagation(){

        orderService.testRequired();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/requiresnew-propagation")
    public ResponseEntity<Void> testRequiresNewPropagation(){

        innerService.testRequiresNew();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/supports-with-transaction-propagation")
    public ResponseEntity<Void> testSupportsPropagation(){

        // to check the proxy
        System.out.println(
                "Is AOP proxy = "
                        + AopUtils.isAopProxy(innerService));

        innerService.testSupportsWithTransaction();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/supports-without-transaction-propagation")
    public ResponseEntity<Void> testSupportsWithoutTransactionPropagation(){


        innerService.testSupportsWithoutTransaction();

        return ResponseEntity.ok().build();
    }
    // testing AOP proxy

    private final ProxyExperimentService proxyExperimentService;

    @GetMapping("/test/test-proxy")
    public ResponseEntity<String> testProxy()
    {
        System.out.println("Is Aop proxy = "+ AopUtils.isAopProxy(proxyExperimentService));
        proxyExperimentService.innerMethod();
        return ResponseEntity.ok("test-proxy successful");
    }

    @GetMapping("/test/test-proxy-self-call")
    public ResponseEntity<String> testProxySelfCall()
    {
        System.out.println("Is Aop proxy = "+ AopUtils.isAopProxy(proxyExperimentService));
        proxyExperimentService.outerMethod();
        return ResponseEntity.ok("test-proxy-self-call successful");
    }

    private final MultiDbTestService multiDbTestService;

    @PostMapping("/test/h2")
    public ResponseEntity<Void> testH2() {

        multiDbTestService.saveAudit();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/mysql")
    public ResponseEntity<Void> testMysql() {

        multiDbTestService.saveOrder();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/testTwoDatabases")
    ResponseEntity<Void> testTwoDatabases() {

        multiDbTestService.testTwoDatabases();

        return ResponseEntity.ok().build();
    }



}