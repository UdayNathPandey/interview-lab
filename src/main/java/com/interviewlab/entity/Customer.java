package com.interviewlab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="customer_name",
    nullable=false,
    length=50)
    private String name;

    @Column(name="customer_email",
    nullable=false,
    length=50)
    private String email;

    @OneToMany(
            mappedBy="customer",
//            cascade=CascadeType.REMOVE
            orphanRemoval=true
//            ,fetch=FetchType.EAGER
            ,fetch=FetchType.LAZY
    )
    private List<Order> orders;
}


/***
 * INSERT INTO customer (id, customer_email, customer_name)
 * VALUES (10, 'john.doe@example.com', 'John Doe');
 *
 *
 * INSERT INTO customer (id, customer_email, customer_name)
 * VALUES (11, 'jane.smith@example.com', 'Jane Smith');
 *
 *
 */