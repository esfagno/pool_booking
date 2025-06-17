package com.poolapp.pool.model;

import jakarta.persistence.*;


import java.time.LocalDateTime;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(nullable = false, length = 20)
    @Size(min = 2, max = 20)
    private String firstName;
    @Column(nullable = false, length = 50)
    @Size(min = 2, max = 50)
    private String lastName;
    private String phoneNumber;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}