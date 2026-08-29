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
            fetch=FetchType.LAZY
//    fetch=FetchType.EAGER // by default
    )
    @JoinColumn(name="customer_id")
    private Customer customer;

    // testing optimistic locking -> will aut increment version with every update on that row
    @Version
    private Long version;

}


/***
 * INSERT INTO orders (id, amount, created_at, customer_email, customer_name, status, updated_at, discount, customer_id)
 * VALUES
 * (101, 1500.00, NOW(), 'john.doe@example.com', 'John Doe', 'CONFIRMED', NOW(), 100.00, 10),
 * (102, 3450.50, NOW(), 'john.doe@example.com', 'John Doe', 'PENDING', NOW(), 0.00, 10),
 * (103, 999.99, NOW(), 'john.doe@example.com', 'John Doe', 'CONFIRMED', NOW(), 50.00, 10);
 *
 *
 *
 * INSERT INTO orders (id, amount, created_at, customer_email, customer_name, status, updated_at, discount, customer_id)
 * VALUES
 * (201, 1200.50, NOW(), 'jane.smith@example.com', 'Jane Smith', 'CONFIRMED', NOW(), 120.00, 11),
 * (202, 450.00, NOW(), 'jane.smith@example.com', 'Jane Smith', 'PENDING', NOW(), 0.00, 11),
 * (203, 899.99, NOW(), 'jane.smith@example.com', 'Jane Smith', 'CANCELLED', NOW(), 50.00, 11);
 *
 *
 */