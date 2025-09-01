package com.poolapp.pool.service.impl.validation;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.service.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(3)
public class UserSubscriptionValidator implements BookingValidator {
    private final UserSubscriptionService userSubscriptionService;

    @Override
    public void validate(BookingDTO bookingDTO) {
        if (bookingDTO.getUserSubscriptionDTO() != null) {
            userSubscriptionService.validateUserSubscription(bookingDTO.getUserEmail());
        }
    }
}