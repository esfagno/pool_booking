package com.poolapp.pool.repository.specification;

import com.poolapp.pool.model.Session;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class SessionSpecification {

    public static Specification<Session> hasPoolName(String poolName) {
        return (root, query, cb) -> {
            if (poolName == null || poolName.isBlank()) {
                return null;
            }
            Join<Object, Object> poolJoin = root.join("pool");
            return cb.equal(cb.lower(poolJoin.get("name")), poolName.toLowerCase());
        };
    }

    public static Specification<Session> hasStartTime(LocalDateTime startTime) {
        return (root, query, cb) -> {
            if (startTime == null) {
                return null;
            }
            return cb.equal(root.get("startTime"), startTime);
        };
    }

    public static Specification<Session> hasEndTime(LocalDateTime endTime) {
        return (root, query, cb) -> {
            if (endTime == null) {
                return null;
            }
            return cb.equal(root.get("endTime"), endTime);
        };
    }
}

