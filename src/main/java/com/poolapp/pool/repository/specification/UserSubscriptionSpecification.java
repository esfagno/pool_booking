package com.poolapp.pool.repository.specification;

import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.SubscriptionStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class UserSubscriptionSpecification {

    public static Specification<UserSubscription> hasUserEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;

            Join<UserSubscription, User> userJoin = root.join("user");
            return cb.equal(userJoin.get("email"), email);
        };
    }

    public static Specification<UserSubscription> hasSubscriptionStatus(SubscriptionStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            Join<UserSubscription, Subscription> subscriptionJoin = root.join("subscription");
            return cb.equal(subscriptionJoin.get("status"), status);
        };
    }

    public static Specification<UserSubscription> hasSubscriptionTypeName(String typeName) {
        return (root, query, cb) -> {
            if (typeName == null || typeName.isBlank()) return null;
            Join<UserSubscription, Subscription> subscriptionJoin = root.join("subscription");
            Join<Subscription, SubscriptionType> typeJoin = subscriptionJoin.join("subscriptionType");
            return cb.equal(typeJoin.get("name"), typeName);
        };
    }

    public static Specification<UserSubscription> hasRemainingBookingsGreaterThan(int remaining) {
        return (root, query, cb) -> cb.greaterThan(root.get("remainingBookings"), remaining);
    }

    public static Specification<UserSubscription> isActiveAndHasRemainingBookings(int minBookings) {
        return (root, query, cb) -> {
            Join<UserSubscription, Subscription> subscriptionJoin = root.join("subscription");
            return cb.and(cb.equal(subscriptionJoin.get("status"), SubscriptionStatus.ACTIVE), cb.greaterThan(root.get("remainingBookings"), minBookings));
        };
    }

    public static Specification<UserSubscription> isAssignedBefore(LocalDateTime time) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("assignedAt"), time);
    }

}
