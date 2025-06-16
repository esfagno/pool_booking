package com.poolapp.pool.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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
    private Integer  id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}