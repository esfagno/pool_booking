package com.poolapp.pool.repository.custom;

import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.BookingId;
import com.poolapp.pool.model.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepositoryCustom {

    List<Booking> findBookingsByFilter(BookingId bookingId, BookingStatus status, String poolName, LocalDateTime startTime);

}
