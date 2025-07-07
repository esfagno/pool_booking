package com.poolapp.pool.repository.specification;

import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.model.enums.SubscriptionStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class SubscriptionSpecification {

    public static Specification<Subscription> hasStatus(SubscriptionStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Subscription> hasSubscriptionTypeName(String name) {
        return (root, query, cb) -> {
            if (name == null) return null;
            Join<Subscription, SubscriptionType> joinType = root.join("subscriptionType");
            return cb.equal(joinType.get("name"), name);
        };
    }
}
