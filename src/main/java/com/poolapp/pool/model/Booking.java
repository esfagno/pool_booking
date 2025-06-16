package com.poolapp.pool.model;

import com.poolapp.pool.model.enums.BookingStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;
    private LocalDateTime bookingTime;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}