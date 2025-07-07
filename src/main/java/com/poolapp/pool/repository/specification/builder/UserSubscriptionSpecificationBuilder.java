package com.poolapp.pool.repository.specification.builder;

import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import static com.poolapp.pool.repository.specification.UserSubscriptionSpecification.hasSubscriptionStatus;
import static com.poolapp.pool.repository.specification.UserSubscriptionSpecification.hasSubscriptionTypeName;
import static com.poolapp.pool.repository.specification.UserSubscriptionSpecification.hasUserEmail;

@Component
public class UserSubscriptionSpecificationBuilder {

    public Specification<UserSubscription> buildSpecification(UserSubscription filter) {
        Specification<UserSubscription> spec = Specification.where(null);

        if (filter == null) {
            return spec;
        }

        String userEmail = filter.getUser().getEmail();
        if (userEmail != null && !userEmail.isBlank()) {
            spec = spec.and(hasUserEmail(userEmail));
        }

        if (filter.getSubscription() != null) {
            SubscriptionStatus status = filter.getSubscription().getStatus();
            if (status != null) {
                spec = spec.and(hasSubscriptionStatus(status));
            }

            if (filter.getSubscription().getSubscriptionType() != null) {
                String typeName = filter.getSubscription().getSubscriptionType().getName();
                if (typeName != null && !typeName.isBlank()) {
                    spec = spec.and(hasSubscriptionTypeName(typeName));
                }
            }
        }

        return spec;
    }
}
