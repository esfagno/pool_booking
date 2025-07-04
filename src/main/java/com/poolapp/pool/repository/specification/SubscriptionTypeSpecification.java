package com.poolapp.pool.repository.specification;

import com.poolapp.pool.model.SubscriptionType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class SubscriptionTypeSpecification {

    public static Specification<SubscriptionType> hasName(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<SubscriptionType> hasMaxBookingsPerMonth(Integer maxBookingsPerMonth) {
        return (root, query, cb) -> cb.equal(root.get("maxBookingsPerMonth"), maxBookingsPerMonth);
    }

    public static Specification<SubscriptionType> hasPrice(BigDecimal price) {
        return (root, query, cb) -> cb.equal(root.get("price"), price);
    }

    public static Specification<SubscriptionType> hasDurationDays(Integer durationDays) {
        return (root, query, cb) -> cb.equal(root.get("durationDays"), durationDays);
    }

    public static Specification<SubscriptionType> hasDescription(String description) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }
}
