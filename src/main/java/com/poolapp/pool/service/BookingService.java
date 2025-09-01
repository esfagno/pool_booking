package com.poolapp.pool.service;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.dto.requestDTO.RequestBookingDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    BookingDTO createBooking(BookingDTO bookingDTO);

    BookingDTO updateBooking(BookingDTO bookingDTO, BookingDTO newBookingDTO);

    boolean hasUserBookedForSession(String userEmail, String poolName, LocalDateTime startTime);

    void expirePastBookings(LocalDateTime now);

    void cancelBooking(BookingDTO bookingDTO);

    void deleteBooking(BookingDTO bookingDTO);

    List<BookingDTO> findBookingsByFilter(RequestBookingDTO requestBookingDTO);

    void deleteBookingsBySession(SessionDTO sessionDTO);

    Long countBookingsBySession(SessionDTO sessionDTO);


}
