package com.poolapp.pool.repository.specification;

import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.enums.BookingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class BookingSpecification {

    public static Specification<Booking> hasUserId(Integer userId) {
        return (root, query, cb) -> cb.equal(root.get("id").get("userId"), userId);
    }

    public static Specification<Booking> hasSessionId(Integer sessionId) {
        return (root, query, cb) -> cb.equal(root.get("id").get("sessionId"), sessionId);
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> hasPoolName(String poolName) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("session").get("pool").get("name")), poolName.toLowerCase());
    }

    public static Specification<Booking> hasSessionStartTime(LocalDateTime startTime) {
        return (root, query, cb) -> cb.equal(root.get("session").get("startTime"), startTime);
    }

    public static Specification<Booking> hasSubscriptionId(Long subscriptionId) {
        return (root, query, cb) -> cb.equal(root.get("userSubscription").get("id"), subscriptionId);
    }
}

