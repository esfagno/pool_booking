package com.poolapp.pool.repository.specification.builder;

import com.poolapp.pool.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import static com.poolapp.pool.repository.specification.SubscriptionSpecification.hasStatus;
import static com.poolapp.pool.repository.specification.SubscriptionSpecification.hasSubscriptionTypeName;

@Component
@RequiredArgsConstructor
public class SubscriptionSpecificationBuilder {

    public Specification<Subscription> buildSpecification(Subscription filter) {
        Specification<Subscription> spec = Specification.where(null);

        if (filter.getStatus() != null) {
            spec = spec.and(hasStatus(filter.getStatus()));
        }

        String typeName = filter.getSubscriptionType() != null
                ? filter.getSubscriptionType().getName()
                : null;
        if (typeName != null) {
            spec = spec.and(hasSubscriptionTypeName(typeName));
        }

        return spec;
    }
}
