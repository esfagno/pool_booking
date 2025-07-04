package com.poolapp.pool.repository.specification.builder;

import com.poolapp.pool.model.Pool;
import com.poolapp.pool.repository.specification.PoolSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PoolSpecificationBuilder {
    public Specification<Pool> buildSpecification(Pool filter) {
        Specification<Pool> spec = Specification.where(null);

        if (filter.getName() != null && !filter.getName().isBlank()) {
            spec = spec.and(PoolSpecification.hasNameLike(filter.getName()));
        }

        if (filter.getAddress() != null && !filter.getAddress().isBlank()) {
            spec = spec.and(PoolSpecification.hasAddressLike(filter.getAddress()));
        }

        if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
            spec = spec.and(PoolSpecification.hasDescriptionLike(filter.getDescription()));
        }

        if (filter.getMaxCapacity() != null) {
            spec = spec.and(PoolSpecification.hasMaxCapacity(filter.getMaxCapacity()));
        }

        if (filter.getSessionDurationMinutes() != null) {
            spec = spec.and(PoolSpecification.hasSessionDuration(filter.getSessionDurationMinutes()));
        }

        return spec;
    }
}

