package com.poolapp.pool.repository.custom;

import com.poolapp.pool.model.Booking;

import java.util.List;

public interface BookingRepositoryCustom {

    List<Booking> findBookingsByFilter(Booking filter);

}
