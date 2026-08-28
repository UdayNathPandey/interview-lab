package com.interviewlab.service;

import com.interviewlab.entity.Customer;
import com.interviewlab.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.interviewlab.utility.Utility;

@Service
@RequiredArgsConstructor
public class CustomerServiceImp implements CustomerService{

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createCustomer() {

        Customer customer = new Customer();
        customer.setName("Required Customer");
        customer.setEmail("required@gmail.com");

        customerRepository.save(customer);

        System.out.println("CustomerService completed");
    }




}
