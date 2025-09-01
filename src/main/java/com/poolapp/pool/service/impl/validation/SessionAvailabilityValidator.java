package com.poolapp.pool.service.impl.validation;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class SessionAvailabilityValidator implements BookingValidator {
    private final SessionService sessionService;

    @Override
    public void validate(BookingDTO bookingDTO) {
        sessionService.validateSessionHasAvailableSpots(bookingDTO.getSessionDTO());
    }
}