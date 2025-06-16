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
@Table(name = "session")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "pool_id")
    private Pool pool;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int currentCapacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}