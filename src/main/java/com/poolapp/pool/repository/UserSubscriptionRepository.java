package com.poolapp.pool.repository;

import com.poolapp.pool.model.UserSubscription;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer>, JpaSpecificationExecutor<UserSubscription> {

    @Override
    @EntityGraph(attributePaths = {"user", "subscription.subscriptionType"})
    Optional<UserSubscription> findOne(Specification<UserSubscription> spec);
}
