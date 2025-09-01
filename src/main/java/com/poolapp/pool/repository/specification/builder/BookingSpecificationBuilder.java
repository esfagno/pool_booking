package com.poolapp.pool.repository.specification.builder;

import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.UserSubscription;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingSpecificationBuilder {

    public Specification<Booking> buildSpecification(Booking filter, String userEmail) {
        return (root, query, cb) -> {
            Join<Booking, Session> sessionJoin = root.join("session", JoinType.INNER);
            Join<Session, Pool> poolJoin = sessionJoin.join("pool", JoinType.INNER);
            Join<Booking, User> bookingUserJoin = root.join("user", JoinType.INNER);
            Join<Booking, UserSubscription> usJoin = root.join("userSubscription", JoinType.LEFT);

            Predicate predicate = cb.conjunction();

            if (filter.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), filter.getStatus()));
            }

            if (StringUtils.hasText(userEmail)) {
                predicate = cb.and(predicate,
                        cb.equal(cb.lower(bookingUserJoin.get("email")), userEmail.toLowerCase().trim()));
            }

            if (filter.getUserSubscription() != null && filter.getUserSubscription().getId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(usJoin.get("id"), filter.getUserSubscription().getId()));
            }

            if (filter.getSession() != null && filter.getSession().getPool() != null &&
                    StringUtils.hasText(filter.getSession().getPool().getName())) {
                String poolName = filter.getSession().getPool().getName().toLowerCase();
                predicate = cb.and(predicate,
                        cb.like(cb.lower(poolJoin.get("name")), "%" + poolName + "%"));
            }

            if (filter.getSession() != null) {
                LocalDateTime start = filter.getSession().getStartTime();
                LocalDateTime end = filter.getSession().getEndTime();

                if (start != null && end != null) {
                    predicate = cb.and(predicate,
                            cb.lessThanOrEqualTo(sessionJoin.get("startTime"), end),
                            cb.greaterThanOrEqualTo(sessionJoin.get("endTime"), start));
                } else if (start != null) {
                    predicate = cb.and(predicate,
                            cb.greaterThanOrEqualTo(sessionJoin.get("endTime"), start));
                } else if (end != null) {
                    predicate = cb.and(predicate,
                            cb.lessThanOrEqualTo(sessionJoin.get("startTime"), end));
                }
            }

            return predicate;
        };
    }
}
