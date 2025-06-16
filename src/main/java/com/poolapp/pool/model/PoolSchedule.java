package com.poolapp.pool.model;

import jakarta.persistence.*;

import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pool_schedule")
public class PoolSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer  id;
    @ManyToOne
    @JoinColumn(name = "pool_id")
    private Pool pool;
    private Short dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
}