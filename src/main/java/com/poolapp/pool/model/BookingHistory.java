package com.poolapp.pool.model;

import com.poolapp.pool.model.enums.BookingAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "booking_history")
public class BookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Booking booking;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingAction action;

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private String notes;
}
