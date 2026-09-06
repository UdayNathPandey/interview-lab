package com.interviewlab.entity.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AppUser {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    @Column(nullable=false , unique=true, length=50)
    private String username;

    @Column(nullable=false, length=250)
    private String password;

    @Column(nullable=false, length=20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable=false)
    private boolean enabled = true;


}
