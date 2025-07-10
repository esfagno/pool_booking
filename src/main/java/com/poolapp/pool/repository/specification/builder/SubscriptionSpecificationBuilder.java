package com.poolapp.pool.repository.specification.builder;

import com.poolapp.pool.dto.SubscriptionDTO;
import com.poolapp.pool.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import static com.poolapp.pool.repository.specification.SubscriptionSpecification.hasStatus;
import static com.poolapp.pool.repository.specification.SubscriptionSpecification.hasSubscriptionTypeName;

@Component
@RequiredArgsConstructor
public class SubscriptionSpecificationBuilder {

    public Specification<Subscription> buildSpecification(SubscriptionDTO filterDTO) {
        Specification<Subscription> spec = Specification.where(null);

        if (filterDTO.getStatus() != null) {
            spec = spec.and(hasStatus(filterDTO.getStatus()));
        }

        String typeName = filterDTO.getSubscriptionTypeDTO() != null
                ? filterDTO.getSubscriptionTypeDTO().getName()
                : null;
        if (typeName != null) {
            spec = spec.and(hasSubscriptionTypeName(typeName));
        }

        return spec;
    }
}
