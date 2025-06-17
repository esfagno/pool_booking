package com.poolapp.pool.model;

import jakarta.persistence.*;

import java.time.LocalTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pool_schedule", uniqueConstraints = @UniqueConstraint(columnNames = {"pool_id", "day_of_week"}))
public class PoolSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "pool_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Pool pool;

    @Column(name = "day_of_week", nullable = false)
    @Min(1)
    @Max(7)
    private Short dayOfWeek;

    @Column(nullable = false)
    private LocalTime openingTime;

    @Column(nullable = false)
    private LocalTime closingTime;

    @AssertTrue
    private boolean isOpeningBeforeClosing() {
        if (openingTime == null || closingTime == null) return true;
        return openingTime.isBefore(closingTime);
    }

}