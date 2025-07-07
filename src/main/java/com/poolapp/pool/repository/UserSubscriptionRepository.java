package com.poolapp.pool.repository;

import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer>, JpaSpecificationExecutor<UserSubscription> {
    Optional<UserSubscription> findByUserEmail(String name);

    Optional<UserSubscription> findByUserEmailAndSubscriptionId(String userEmail, Integer subscriptionId);


    /*
     i can’t use a specification here because Hibernate can’t properly
     compare enum fields with string parameters in Criteria queries.
     i’m not sure how to cast or map the string to the enum in a specification,
     so it doesn’t work
     */
    @Query(value = """
                SELECT us.* 
                FROM user_subscription us
                JOIN users u ON us.user_id = u.id
                JOIN subscription s ON us.subscription_id = s.id
                WHERE u.email = :email
                  AND s.status = CAST(:status AS subscription_status)
                  AND us.remaining_bookings > :minBookings
                LIMIT 1
            """, nativeQuery = true)
    Optional<UserSubscription> findByUserEmailAndSubscriptionStatusAndRemainingBookingsGreaterThanNative(
            @Param("email") String email,
            @Param("status") String status,
            @Param("minBookings") int minBookings
    );

    List<UserSubscription> findBySubscription_Status(SubscriptionStatus status);

}
