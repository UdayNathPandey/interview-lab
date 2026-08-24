package com.interviewlab.service;

import com.interviewlab.dto.CreateOrderRequest;
import com.interviewlab.dto.OrderResponse;
import com.interviewlab.dto.PatchOrderRequest;
import com.interviewlab.dto.UpdateOrderRequest;
import com.interviewlab.entity.Customer;
import com.interviewlab.entity.OrderStatus;
import com.interviewlab.exception.ResourceNotFoundException;
import com.interviewlab.repository.CustomerRepository;
import com.interviewlab.repository.OrderRepository;
//import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.interviewlab.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImp implements OrderService{

//    @Autowired // recommended to use constructor injection in production
    private final OrderRepository orderRepository;
 // instead of writing constructor -> using @RequiredArgsConstructor
//    public OrderServiceImp(OrderRepository orderRepository)
//    {
//        this.orderRepository=orderRepository;
//    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest orderRequestDto) {

        // name, email and amount
        LocalDateTime now = LocalDateTime.now(); // agr create and update me ye function likhenge to time difference ho skta h
        Order order = Order
                .builder()
                .customerName(orderRequestDto.getCustomerName())
                .customerEmail(orderRequestDto.getCustomerEmail())
                .amount(orderRequestDto.getAmount())
                .status(OrderStatus.PENDING) // missed it
                .createdAt(now) // missed it
                .updatedAt(now) // missed it
                .build();
        Order orderCreated = orderRepository.save(order);

        return OrderResponse.builder()
                .id(orderCreated.getId())
                .customerName(orderCreated.getCustomerName())
                .customerEmail(orderCreated.getCustomerEmail())
                .amount(orderCreated.getAmount())
                .status(orderCreated.getStatus())
                .createdAt(orderCreated.getCreatedAt())
                .updatedAt(orderCreated.getUpdatedAt())
                .build();
    }

    @Override
    public OrderResponse getOrderById(Long id){

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: "+id));
        // orElseThrow lambda leta h ye miss hua
        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Override
    public List<OrderResponse> getAllOrders()
    {
//        List<Order> allOrders = orderRepository.findAll();
//        List<OrderResponse> allOrderResponse=new ArrayList<>();
//        for(Order order : allOrders)
//        {
//            OrderResponse orderResponse= OrderResponse.builder()
//                    .id(order.getId())
//                    .customerName(order.getCustomerName())
//                    .customerEmail(order.getCustomerEmail())
//                    .amount(order.getAmount())
//                    .status(order.getStatus())
//                    .createdAt(order.getCreatedAt())
//                    .updatedAt(order.getUpdatedAt())
//                    .build();
//
//        }
        // yha maine by default for loop se solve kra , should have though about stream while processing collection
        return orderRepository.findAll().stream()
                .map(order ->
                       OrderResponse.builder()
                        .id(order.getId())
                        .customerName(order.getCustomerName())
                        .customerEmail(order.getCustomerEmail())
                        .amount(order.getAmount())
                        .status(order.getStatus())
                        .createdAt(order.getCreatedAt())
                        .updatedAt(order.getUpdatedAt())
                        .build()
                ).toList(); // toList() belongs to 16+
    }

    @Override
    public OrderResponse updateOrder(Long id, UpdateOrderRequest updateOrderRequest)
    {
        //check if requested user exist or not
        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with Id "+id));

        // set the field of the fetched order with updated ones
        order.setCustomerName(updateOrderRequest.getCustomerName());
        order.setCustomerEmail(updateOrderRequest.getCustomerEmail());
        order.setAmount(updateOrderRequest.getAmount());
        order.setDiscount(updateOrderRequest.getDiscount());
        order.setStatus(updateOrderRequest.getStatus());
        order.setUpdatedAt(LocalDateTime.now());
        // save the updated order in database
        Order updatedOrder = orderRepository.save(order);
        //return OrderResponse
        return OrderResponse.builder()
                .id(updatedOrder.getId())
                .customerName(updatedOrder.getCustomerName())
                .customerEmail(updatedOrder.getCustomerEmail())
                .amount(updatedOrder.getAmount())
                .discount(updatedOrder.getDiscount())
                .status(updatedOrder.getStatus())
                .createdAt(updatedOrder.getCreatedAt())
                .updatedAt(updatedOrder.getUpdatedAt())
                .build();
    }

    @Override
    public OrderResponse patchOrder(Long id , PatchOrderRequest patchOrderRequest)
    {
        //check if user exits
        Order fetchedOrder = orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order not found with id "+id));
        //update the user
        if (patchOrderRequest.getCustomerName() != null) {
            fetchedOrder.setCustomerName(patchOrderRequest.getCustomerName());
        }

        if (patchOrderRequest.getCustomerEmail() != null) {
            fetchedOrder.setCustomerEmail(patchOrderRequest.getCustomerEmail());
        }

        if (patchOrderRequest.getAmount() != null) {
            fetchedOrder.setAmount(patchOrderRequest.getAmount());
        }

        if (patchOrderRequest.getDiscount() != null) {
            fetchedOrder.setDiscount(patchOrderRequest.getDiscount());
        }

        if (patchOrderRequest.getStatus() != null) {
            fetchedOrder.setStatus(patchOrderRequest.getStatus());
        }
        fetchedOrder.setUpdatedAt(LocalDateTime.now());
        // save the user
        Order patchedOrder=   orderRepository.save(fetchedOrder);
        // return orderResponse
        return OrderResponse.builder()
                .id(patchedOrder.getId())
                .customerName(patchedOrder.getCustomerName())
                .customerEmail(patchedOrder.getCustomerEmail())
                .amount(patchedOrder.getAmount())
                .discount(patchedOrder.getDiscount())
                .status(patchedOrder.getStatus())
                .createdAt(patchedOrder.getCreatedAt())
                .updatedAt(patchedOrder.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteOrder(Long id){
        Order fetchOrder = orderRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Order not found with id "+id));
        orderRepository.delete(fetchOrder);

//        return null; // Void k liye null return krte h ye nhi pta tha
        // production me void recommended h
    }

    // testing jpa internal working
    @Autowired
    private CustomerRepository customerRepository;

    public void testCustomerOrderRelationship()
    {
        //create and save a customer
        Customer customer=new Customer();
        customer.setName("Uday");
        customer.setEmail("uday@gmail.com");

//        Customer savedCustomer = customerRepository.save(customer);

        // create and save an order for that customer
        Order order = Order.builder()
                .customer(customer)
                .customerEmail(customer.getEmail())
                .customerName(customer.getName())
                .amount(new BigDecimal("2500.00"))
                .discount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

    }

    @Transactional // this is required for dirty checking to work
    public void testDirtyChecking(Long id)
    {
        Order order = orderRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Order not found for id "+id));
        System.out.println(
                "Before = " + order.getAmount()
        );

        order.setAmount(
                new BigDecimal("7778.00")
        );

        System.out.println(
                "After = " + order.getAmount());
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void testDetachedEntity(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow();

        System.out.println(
                "Before detach = "
                        + entityManager.contains(order)
        );

        entityManager.detach(order);

        System.out.println(
                "After detach = "
                        + entityManager.contains(order)
        );

        order.setAmount(new BigDecimal("1111.00"));
    }

    @Transactional
    public void testMerge(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(("Order not found with id " + id)));
        System.out.println("Before detach:");
        System.out.println("contains = " + entityManager.contains(order)); //true

        entityManager.detach(order);

        System.out.println("After detach:");
        System.out.println("contains = " + entityManager.contains(order)); //false

        order.setAmount(new BigDecimal("4444.00"));

        System.out.println("Detached object amount = "
                + order.getAmount()); //4444.00

        Order mergedOrder = entityManager.merge(order);

        order.setAmount(new BigDecimal("6666.00"));

        System.out.println("After merge:");
        System.out.println("original contains = "
                + entityManager.contains(order)); //false

        System.out.println("merged contains = "
                + entityManager.contains(mergedOrder)); //true

        System.out.println("merged amount = "
                + mergedOrder.getAmount()); //4444.00
        System.out.println("Detached object amount = "
                + order.getAmount()); //6666.00

        System.out.println("same object = " + (order == mergedOrder)); //false

        mergedOrder.setAmount(new BigDecimal("1111.00"));
        System.out.println("merged amount after set = "
                + mergedOrder.getAmount()); //1111.00
        System.out.println("Detached object amount after merged set = "
                + order.getAmount()); //6666.00


    }

    @Transactional
    public void testCascadePersist(){
        Customer customer = Customer.builder()
                .name("Cascade Customer")
                .email("cascade@gmail.com")
                .build();

        Order order = Order.builder()
                .customer(customer)
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .amount(new BigDecimal("5000.00"))
                .discount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);
    }

    @Transactional
    public void testCascadeMerge(Long id)
    {
        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("order not found with id "+id));

        Customer customer = order.getCustomer();

        System.out.println(
                "Order managed = "
                        + entityManager.contains(order));

        System.out.println(
                "Customer managed = "
                        + entityManager.contains(customer));

        entityManager.detach(order);
        entityManager.detach(customer);

        System.out.println(
                "Order after detach = "
                        + entityManager.contains(order));

        System.out.println(
                "Customer after detach = "
                        + entityManager.contains(customer));

        // Modify detached entities

        order.setAmount(new BigDecimal("6000.00"));

        customer.setName("Merged Customer");
        customer.setEmail("merged@gmail.com");

        //merge order
        Order mergedOrder=entityManager.merge(order);

        System.out.println(
                "Merged Customer managed = "
                + entityManager.contains(
                mergedOrder.getCustomer()));

        System.out.println(
                "Merged Customer name = "
                        + mergedOrder.getCustomer().getName());

        System.out.println(
                "Older Customer name == merged customer "
                        + (mergedOrder.getCustomer() == customer));




    }

    @Transactional
    public void testCascadeRemove(Long id)
    {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Customer not found with id "+id));
        customerRepository.delete(customer);
    }

    @Transactional
    public void testCascadeOrphanRemoval(Long cid,Long oid)
    {
            Customer customer=customerRepository.findById(cid)
                    .orElseThrow(()->new ResourceNotFoundException("Customer not found with id "+cid));

            Order order =orderRepository.findById(oid)
                    .orElseThrow(()->new ResourceNotFoundException("Order not found with id"+oid));

            customer.getOrders().remove(order); // ye database se us order ko delete kr dega jo orphan hua h
            // to maintain the bidirectional integraty , lets manually delete the customer from deleted order
            order.setCustomer(null); // ye bs java me h , lekin tbhi usko maintain krna recommended h


    }

}
