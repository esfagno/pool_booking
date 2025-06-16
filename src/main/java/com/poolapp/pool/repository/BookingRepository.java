package com.poolapp.pool.repository;

import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    boolean existsByUserIdAndSession_StartTimeAfter(Long userId, LocalDateTime dateTime);
}
