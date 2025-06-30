package com.poolapp.pool.repository.impl;

import com.poolapp.pool.model.Booking;
import com.poolapp.pool.repository.base.criteria.BookingCriteria;
import com.poolapp.pool.repository.custom.BookingRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryImpl implements BookingRepositoryCustom {

    private final BookingCriteria bookingCriteria;

    public List<Booking> findBookingsByFilter(Booking booking) {
        return bookingCriteria.findBookingsByFilter(booking);
    }

}
