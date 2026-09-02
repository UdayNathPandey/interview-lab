package com.interviewlab.entity.mysql;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name="customer_name",
            nullable=false,
            length=50
    )
    private String customerName;
    @Column(
            name="customer_email",
            nullable=false,
            length=50
    )
    private String customerEmail;
//    private Double amount;
//    Double → floating-point representation
//       → precision problems
    // production me BigDecimal use krna better h
    @Column(name="amount",
            nullable=false,
            precision=15, scale=2) // did not know this
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(nullable=false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(
            name="discount",
            nullable=false,
            precision = 15,
            scale=2
    )
    private BigDecimal discount;

//    @ManyToOne(cascade=CascadeType.PERSIST)
//    @ManyToOne(cascade=CascadeType.MERGE)
    @ManyToOne(
            fetch=FetchType.LAZY // lazy rkhne pr bina dto wala pagination kam nhi krega kyoki json seriallize nhi ho pa rha
//    fetch=FetchType.EAGER // by default
    )
    @JoinColumn(name="customer_id")
//    @Column(nullable = true) // to hablde customer with null id
    private Customer customer;

    // testing optimistic locking -> will aut increment version with every update on that row
    @Version
    private Long version;

}

