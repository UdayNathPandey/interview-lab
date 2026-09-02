package com.interviewlab.entity.mysql;

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
            ,fetch=FetchType.LAZY // lazy rkhne pr bina dto wala pagination kam nhi krega kyoki json seriallize nhi ho pa rha
    )
    private List<Order> orders;
}

