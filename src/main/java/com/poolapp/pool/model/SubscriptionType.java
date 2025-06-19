package com.poolapp.pool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "subscription_type")
@EntityListeners(AuditingEntityListener.class)
public class SubscriptionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String name;

    private String description;

    @Column(name = "max_bookings_per_month", nullable = false)
    @Min(0)
    private Integer maxBookingsPerMonth;

    @Column(nullable = false)
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    @Min(1)
    private Integer durationDays;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
