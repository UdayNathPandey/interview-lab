package com.interviewlab.entity;

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

    @ManyToOne
    @JoinColumn(name="customer_id")
    private Customer customer;


}
