package com.poolapp.pool.repository.specification;

import com.poolapp.pool.model.Pool;
import org.springframework.data.jpa.domain.Specification;

public class PoolSpecification {

    public static Specification<Pool> hasNameLike(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Pool> hasAddressLike(String address) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%");
    }

    public static Specification<Pool> hasDescriptionLike(String description) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<Pool> hasMaxCapacity(Integer maxCapacity) {
        return (root, query, cb) -> cb.equal(root.get("maxCapacity"), maxCapacity);
    }

    public static Specification<Pool> hasSessionDuration(Integer sessionDuration) {
        return (root, query, cb) -> cb.equal(root.get("sessionDurationMinutes"), sessionDuration);
    }
}

