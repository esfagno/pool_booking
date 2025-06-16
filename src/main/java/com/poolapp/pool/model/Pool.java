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
@Table(name = "pool")
public class Pool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer  id;
    private String name;
    private String address;
    private String description;
    private int maxCapacity;
    private int sessionDurationMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
