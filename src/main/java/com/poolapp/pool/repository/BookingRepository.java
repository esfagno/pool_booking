package com.poolapp.pool.repository;

import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.BookingId;
import com.poolapp.pool.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, BookingId>, JpaSpecificationExecutor<Booking> {

    long countBySessionId(Integer sessionId);

    void deleteAllBySessionId(Integer sessionId);

    boolean existsBySessionId(Integer sessionId);

    boolean existsByUserIdAndSessionStartTimeAfter(Integer userId, LocalDateTime startTime);

    List<Booking> findByUser_EmailAndSession_StartTime(String email, LocalDateTime startTime);

    List<Booking> findBySession_StartTimeBeforeAndStatus(LocalDateTime time, BookingStatus status);

}
