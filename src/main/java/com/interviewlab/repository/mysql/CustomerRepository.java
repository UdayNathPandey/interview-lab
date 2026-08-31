package com.interviewlab.repository.mysql;

import com.interviewlab.entity.mysql.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer,Long> {

}
