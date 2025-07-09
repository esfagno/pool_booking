package com.poolapp.pool.repository;

import com.poolapp.pool.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer>, JpaSpecificationExecutor<UserSubscription> {

    Optional<UserSubscription> findByUserEmailAndSubscriptionId(String userEmail, Integer subscriptionId);

}
