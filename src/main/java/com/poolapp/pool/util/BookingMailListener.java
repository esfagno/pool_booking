package com.poolapp.pool.util;

import com.poolapp.pool.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingMailListener {

    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCreated(BookingCreatedEvent event) {
        try {
            mailService.sendBookingConfirmationEmail(event.userEmail(), event.sessionDTO());
            log.debug("Booking confirmation email sent to {}", event.userEmail());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation to {}: {}", event.userEmail(), e.getMessage(), e);
        }
    }
}