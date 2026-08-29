package com.interviewlab.service;


import com.interviewlab.entity.mysql.Customer;
import com.interviewlab.repository.mysql.CustomerRepository;
import com.interviewlab.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InnerService {

    private final OrderService orderService;
    private final Utility utility;
    private final CustomerRepository customerRepository;

    @Transactional
    public void testRequiresNew() {

        utility.printTransactionInfo("A - START");

        Customer customer = new Customer();
        customer.setName("Outer Customer");
        customer.setEmail("outer@gmail.com");

        customerRepository.save(customer);

        orderService.createOrderInNewTransaction();

        utility.printTransactionInfo("A - AFTER B");

        throw new RuntimeException("Outer transaction failed");
    }
    @Transactional
    public void testSupportsWithTransaction() {

        utility.printTransactionInfo("A - START");

        orderService.supportsMethod();

        utility.printTransactionInfo("A - END");
    }

    public void testSupportsWithoutTransaction() {

        utility.printTransactionInfo("A - START");

        orderService.supportsMethod();

        utility.printTransactionInfo("A - END");
    }

}