package com.interviewlab.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String action;

    private String message;
}