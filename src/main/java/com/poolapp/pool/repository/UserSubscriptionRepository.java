package com.poolapp.pool.repository;

import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer>, JpaSpecificationExecutor<UserSubscription> {
    Optional<UserSubscription> findByUserEmail(String name);

    Optional<UserSubscription> findByUserEmailAndSubscriptionId(String userEmail, Integer subscriptionId);

    List<UserSubscription> findBySubscription_Status(SubscriptionStatus status);

}
