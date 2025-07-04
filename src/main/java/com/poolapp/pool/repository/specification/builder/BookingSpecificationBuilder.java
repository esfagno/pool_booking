package com.poolapp.pool.repository.specification.builder;

import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.BookingId;
import com.poolapp.pool.repository.specification.BookingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingSpecificationBuilder {

    public Specification<Booking> buildSpecification(Booking filter) {
        Specification<Booking> spec = Specification.where(null);

        BookingId bookingId = filter.getId();
        if (bookingId != null) {
            if (bookingId.getUserId() != null) {
                spec = spec.and(BookingSpecification.hasUserId(bookingId.getUserId()));
            }
            if (bookingId.getSessionId() != null) {
                spec = spec.and(BookingSpecification.hasSessionId(bookingId.getSessionId()));
            }
        }

        if (filter.getStatus() != null) {
            spec = spec.and(BookingSpecification.hasStatus(filter.getStatus()));
        }

        if (filter.getSession() != null && filter.getSession().getPool() != null && filter.getSession().getPool().getName() != null) {
            spec = spec.and(BookingSpecification.hasPoolName(filter.getSession().getPool().getName()));
        }

        if (filter.getSession() != null && filter.getSession().getStartTime() != null) {
            spec = spec.and(BookingSpecification.hasSessionStartTime(filter.getSession().getStartTime()));
        }

        return spec;
    }
}

