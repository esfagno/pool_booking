package com.poolapp.pool.repository.specification.builder;

import com.poolapp.pool.model.SubscriptionType;
import com.poolapp.pool.repository.specification.SubscriptionTypeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionTypeSpecificationBuilder {

    public Specification<SubscriptionType> buildSpecification(SubscriptionType filter) {
        Specification<SubscriptionType> spec = Specification.where(null);

        if (filter.getName() != null && !filter.getName().isBlank()) {
            spec = spec.and(SubscriptionTypeSpecification.hasName(filter.getName()));
        }

        if (filter.getMaxBookingsPerMonth() != null) {
            spec = spec.and(SubscriptionTypeSpecification.hasMaxBookingsPerMonth(filter.getMaxBookingsPerMonth()));
        }

        if (filter.getPrice() != null) {
            spec = spec.and(SubscriptionTypeSpecification.hasPrice(filter.getPrice()));
        }

        if (filter.getDurationDays() != null) {
            spec = spec.and(SubscriptionTypeSpecification.hasDurationDays(filter.getDurationDays()));
        }

        if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
            spec = spec.and(SubscriptionTypeSpecification.hasDescription(filter.getDescription()));
        }

        return spec;
    }
}
