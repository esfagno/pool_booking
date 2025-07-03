package com.poolapp.pool.repository.specification.builder;

import com.poolapp.pool.model.Session;
import com.poolapp.pool.repository.specification.SessionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionSpecificationBuilder {

    public Specification<Session> buildSpecification(Session filter) {
        Specification<Session> spec = Specification.where(null);

        if (filter != null) {
            if (filter.getPool() != null && filter.getPool().getName() != null) {
                spec = spec.and(SessionSpecification.hasPoolName(filter.getPool().getName()));
            }
            if (filter.getStartTime() != null) {
                spec = spec.and(SessionSpecification.hasStartTime(filter.getStartTime()));
            }
            if (filter.getEndTime() != null) {
                spec = spec.and(SessionSpecification.hasEndTime(filter.getEndTime()));
            }
        }

        return spec;
    }
}

