package com.poolapp.pool.repository;

import com.poolapp.pool.model.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionTypeRepository extends JpaRepository<SubscriptionType, Integer>, JpaSpecificationExecutor<SubscriptionType> {
    Optional<SubscriptionType> findByName(String name);
}