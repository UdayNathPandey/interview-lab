package com.interviewlab.repository;

import com.interviewlab.dto.OrderSummaryDto;
import com.interviewlab.dto.OrderSummaryView;
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

    // DTO Project -> fetch only required columns from the database
    @Query("""
            select new com.interviewlab.dto.OrderSummaryDto(
                o.id,
                o.amount,
                c.name,
                c.email
            )
            from Order o
            join o.customer c
            
            """)
    List<OrderSummaryDto> findOrderSummaries();

//    @Query("""
//        select
//            o.id as orderId,
//            o.amount as amount,
//            c.name as customerName,
//            c.email as customerEmail
//        from Order o
//        join o.customer c
//        """)
    // upr wali query non nested interface projection ki h
    @Query("""
            
            select o.id as orderId,
            o.amount as amount,
            o.customer as customer
            from Order o
            join o.customer c
            
            """)
    List<OrderSummaryView> findOrderSummaryViews();


}
