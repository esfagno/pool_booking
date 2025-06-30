package com.poolapp.pool.service;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.SessionDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    BookingDTO createBooking(BookingDTO bookingDTO);

    BookingDTO updateBooking(BookingDTO bookingDTO, BookingDTO newBookingDTO);

    boolean hasUserBooked(String userEmail, LocalDateTime sessionStartTime);

    void expirePastBookings(LocalDateTime now);

    void cancelBooking(BookingDTO bookingDTO);

    void deleteBooking(BookingDTO bookingDTO);

    List<BookingDTO> findBookingsByFilter(BookingDTO bookingDTO);

    void deleteBookingsBySession(SessionDTO sessionDTO);

    long countBookingsBySession(SessionDTO sessionDTO);
}
