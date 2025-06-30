package com.poolapp.pool.repository.base.criteria;

import com.poolapp.pool.model.Booking;
import com.poolapp.pool.repository.custom.BookingRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingCriteria implements BookingRepositoryCustom {

    private final EntityManager entityManager;


    @Override
    public List<Booking> findBookingsByFilter(Booking booking) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Booking> query = cb.createQuery(Booking.class);
        Root<Booking> bookingRoot = query.from(Booking.class);

        Join<Object, Object> sessionJoin = bookingRoot.join("session");
        Join<Object, Object> poolJoin = sessionJoin.join("pool");
        Join<Object, Object> userJoin = bookingRoot.join("user");

        List<Predicate> predicates = new ArrayList<>();

        if (booking.getId() != null) {
            if (booking.getId().getUserId() != null) {
                predicates.add(cb.equal(bookingRoot.get("id").get("userId"), booking.getId().getUserId()));
            }
            if (booking.getId().getSessionId() != null) {
                predicates.add(cb.equal(bookingRoot.get("id").get("sessionId"), booking.getId().getSessionId()));
            }
        }

        if (booking.getStatus() != null) {
            predicates.add(cb.equal(bookingRoot.get("status"), booking.getStatus()));
        }

        if (booking.getSession().getPool().getName() != null && !booking.getSession().getPool().getName().isBlank()) {
            predicates.add(cb.equal(cb.lower(poolJoin.get("name")), booking.getSession().getPool().getName().toLowerCase()));
        }

        if (booking.getSession().getStartTime() != null) {
            predicates.add(cb.equal(sessionJoin.get("startTime"), booking.getSession().getStartTime()));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}
