package com.poolapp.pool.repository.specification;

import com.poolapp.pool.model.Role;
import com.poolapp.pool.model.enums.RoleType;
import org.springframework.data.jpa.domain.Specification;

public class RoleSpecification {

    public static Specification<Role> hasName(RoleType roleType) {
        return (root, query, cb) -> {
            if (roleType == null) return null;
            return cb.equal(root.get("name").as(String.class), roleType.name());
        };
    }

    public static Specification<Role> hasDescription(String description) {
        return (root, query, cb) -> {
            if (description == null) return null;
            return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }
}


