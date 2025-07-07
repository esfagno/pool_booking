package com.poolapp.pool.repository;

import com.poolapp.pool.model.Subscription;
import com.poolapp.pool.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer>, JpaSpecificationExecutor<Subscription> {
    Optional<Subscription> findByStatusAndSubscriptionType_Name(SubscriptionStatus status, String name);
}
