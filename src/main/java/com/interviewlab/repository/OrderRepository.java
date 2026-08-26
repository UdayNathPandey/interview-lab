package com.interviewlab.repository;

import com.interviewlab.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

//    @Query("""
//            select o
//            from Order o
//            left join fetch o.customer
//            where o.status = :status
//            """)
    @Query("""
            select o
            from Order o
            join fetch o.customer
            """)
    List<Order> findAllWithCustomer();

    @EntityGraph(attributePaths="customer")
    List<Order> findAll();
}
