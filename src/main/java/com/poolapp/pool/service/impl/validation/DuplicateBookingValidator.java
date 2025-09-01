package com.poolapp.pool.service.impl.validation;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.exception.EntityAlreadyExistsException;
import com.poolapp.pool.model.enums.BookingStatus;
import com.poolapp.pool.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class DuplicateBookingValidator implements BookingValidator {
    private final BookingRepository bookingRepository;

    @Override
    public void validate(BookingDTO bookingDTO) {
        boolean activeBookingExists = bookingRepository
                .existsByUser_EmailAndSession_Pool_NameAndSession_StartTimeAndStatus(
                        bookingDTO.getUserEmail(),
                        bookingDTO.getSessionDTO().getPoolName(),
                        bookingDTO.getSessionDTO().getStartTime(),
                        BookingStatus.ACTIVE
                );

        if (activeBookingExists) {
            throw new EntityAlreadyExistsException(
                    String.format("User %s already has ACTIVE booking for session at %s in %s",
                            bookingDTO.getUserEmail(),
                            bookingDTO.getSessionDTO().getStartTime(),
                            bookingDTO.getSessionDTO().getPoolName()
                    )
            );
        }
    }
}