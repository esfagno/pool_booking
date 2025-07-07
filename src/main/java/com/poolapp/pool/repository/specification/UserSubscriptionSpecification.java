package com.poolapp.pool.repository.specification;

import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.SubscriptionStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class UserSubscriptionSpecification {

    public static Specification<UserSubscription> hasUserEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.equal(root.get("user").get("email"), email);
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
}
