package com.poolapp.pool.model;

import com.poolapp.pool.model.enums.BookingAction;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
