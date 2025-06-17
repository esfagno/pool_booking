package com.poolapp.pool.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscription_type")
public class SubscriptionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer  id;
    private String name;
    private String description;
    private int maxBookingsPerMonth;
    private BigDecimal price;
    private int durationDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}