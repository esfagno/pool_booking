package com.poolapp.pool.model;

import com.poolapp.pool.model.enums.BookingAction;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "booking_history")
public class BookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    @Enumerated(EnumType.STRING)
    private BookingAction action;
    @ManyToOne
    @JoinColumn(name = "changed_by")
    private User changedBy;
    private LocalDateTime createdAt;
    private String notes;
}
