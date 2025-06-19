package com.poolapp.pool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;

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