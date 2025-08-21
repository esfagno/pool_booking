package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.dto.requestDTO.RequestBookingDTO;
import com.poolapp.pool.exception.BookingStatusNotActiveException;
import com.poolapp.pool.exception.EntityAlreadyExistsException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.BookingMapper;
import com.poolapp.pool.mapper.SessionMapperImpl;
import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.BookingStatus;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.specification.builder.BookingSpecificationBuilder;
import com.poolapp.pool.service.BookingService;
import com.poolapp.pool.service.SessionService;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.service.UserSubscriptionService;
import com.poolapp.pool.util.BookingContext;
import com.poolapp.pool.util.BookingContextBuilder;
import com.poolapp.pool.util.BookingCreatedEvent;
import com.poolapp.pool.util.CapacityOperation;
import com.poolapp.pool.util.ChangeSessionCapacityRequest;
import com.poolapp.pool.util.exception.ApiErrorCode;
import com.poolapp.pool.util.exception.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final SessionService sessionService;
    private final BookingSpecificationBuilder bookingSpecificationBuilder;
    private final UserSubscriptionService userSubscriptionService;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingContextBuilder bookingContextBuilder;
    private final SessionMapperImpl sessionMapperImpl;

    @Transactional
    @Override
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        log.debug("Creating booking request for user: {}, session: {}",
                bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        Optional<Booking> cancelledBooking = findCancelledBookingForSession(bookingDTO);

        if (cancelledBooking.isPresent()) {
            return reactivateCancelledBooking(cancelledBooking.get(), bookingDTO);
        }

        validateBookingRequest(bookingDTO);
        Booking booking = createAndPersistBooking(bookingDTO);
        updateRelatedEntities(booking);
        publishBookingEvent(bookingDTO);

        log.info("New booking created successfully for user: {}, session: {}",
                bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
        return bookingMapper.toDto(booking);
    }

    @Transactional
    @Override
    public void deleteBooking(BookingDTO bookingDTO) {
        log.debug("Deleting booking for user: {}, session: {}",
                bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        Booking booking = findBookingOrThrow(bookingDTO);
        handleSubscriptionOnDeletion(booking);

        bookingRepository.deleteById(booking.getId());
        changeSessionCapacity(bookingDTO.getSessionDTO(), CapacityOperation.INCREASE);

        log.info("Booking deleted successfully for user: {}, session: {}",
                bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
    }

    @Transactional
    @Override
    public void cancelBooking(BookingDTO bookingDTO) {
        log.debug("Cancelling booking for user: {}, session: {}",
                bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        Booking booking = findBookingOrThrow(bookingDTO);
        validateBookingCanBeCancelled(booking);

        handleSubscriptionOnCancellation(booking);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        changeSessionCapacity(bookingDTO.getSessionDTO(), CapacityOperation.INCREASE);

        log.info("Booking cancelled successfully for user: {}, session: {}",
                bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
    }

    @Override
    public List<BookingDTO> findBookingsByFilter(RequestBookingDTO filterDTO) {
        log.debug("Finding bookings by filter: {}", filterDTO);

        Booking filter = bookingMapper.toEntity(filterDTO);
        log.debug("Finding bookings by filter: {}", filter.toString());

        Specification<Booking> spec = bookingSpecificationBuilder.buildSpecification(filter, filterDTO.getUserEmail());
        List<Booking> bookings = bookingRepository.findAll(spec);
        log.debug("Found {} bookings with given filter", bookings.size());
        return bookingMapper.toDtoList(bookings);
    }

    @Override
    public BookingDTO updateBooking(BookingDTO bookingDTO, BookingDTO newBookingDTO) {
        log.debug("Updating booking: {}", bookingDTO);

        Booking booking = findBookingOrThrow(bookingDTO);
        bookingMapper.updateBookingFromDto(booking, newBookingDTO);
        Booking saved = bookingRepository.save(booking);

        log.info("Booking updated successfully: id={}", saved.getId());
        return bookingMapper.toDto(saved);
    }

    @Override
    public boolean hasUserBookedForSession(String userEmail, String poolName, LocalDateTime startTime) {
        log.debug("Checking duplicate booking: user={}, pool={}, time={}", userEmail, poolName, startTime);
        boolean exists = !bookingRepository
                .findByUser_EmailAndSession_Pool_NameAndSession_StartTime(userEmail, poolName, startTime)
                .isEmpty();
        log.debug("Duplicate booking check result: {}", exists);
        return exists;
    }

    @Transactional
    @Override
    public void expirePastBookings(LocalDateTime now) {
        log.debug("Expiring past bookings before: {}", now);
        List<Booking> expired = bookingRepository.findBySession_StartTimeBeforeAndStatus(now, BookingStatus.ACTIVE);
        expired.forEach(b -> b.setStatus(BookingStatus.COMPLETED));
        bookingRepository.saveAll(expired);
        log.info("Expired {} bookings", expired.size());
    }

    @Override
    public void deleteBookingsBySession(SessionDTO sessionDTO) {
        log.debug("Deleting bookings by session: {}", sessionDTO);
        Session session = sessionService.getSessionByPoolNameAndStartTime(
                sessionDTO.getPoolName(),
                sessionDTO.getStartTime()
        ).orElseThrow(() -> new ModelNotFoundException(
                ApiErrorCode.NOT_FOUND,
                "pool=" + sessionDTO.getPoolName() + ", startTime=" + sessionDTO.getStartTime()
        ));

        bookingRepository.deleteAllBySessionId(session.getId());
        log.info("Bookings deleted for session id={}", session.getId());
    }

    @Override
    public Long countBookingsBySession(SessionDTO sessionDTO) {
        log.debug("Counting bookings by session: {}", sessionDTO);
        Session session = sessionService.getSessionByPoolNameAndStartTime(
                sessionDTO.getPoolName(),
                sessionDTO.getStartTime()
        ).orElseThrow(() -> new ModelNotFoundException(
                ApiErrorCode.NOT_FOUND,
                "pool=" + sessionDTO.getPoolName() + ", startTime=" + sessionDTO.getStartTime()
        ));

        long count = bookingRepository.countBySessionId(session.getId());
        log.debug("Counted {} bookings for session id={}", count, session.getId());
        return count;
    }

    private BookingDTO reactivateCancelledBooking(Booking existingBooking, BookingDTO bookingDTO) {
        log.info("Reactivating CANCELLED booking id={} for user={}, session={}",
                existingBooking.getId(), bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        validateSessionAvailability(bookingDTO.getSessionDTO());
        validateUserSubscription(bookingDTO.getUserEmail());

        existingBooking.setStatus(BookingStatus.ACTIVE);
        existingBooking.setBookingTime(LocalDateTime.now());

        updateRelatedEntities(existingBooking);

        Booking updated = bookingRepository.save(existingBooking);
        publishBookingEvent(bookingMapper.toDto(updated));

        return bookingMapper.toDto(updated);
    }

    private Booking findBookingOrThrow(BookingDTO bookingDTO) {
        BookingContext context = bookingContextBuilder.build(
                bookingDTO.getUserEmail(),
                bookingDTO.getSessionDTO()
        );

        return bookingRepository.findById(context.getBookingId())
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        String.format("Booking not found: user=%s, startTime=%s",
                                bookingDTO.getUserEmail(),
                                bookingDTO.getSessionDTO().getStartTime()
                        )
                ));
    }

    private void changeSessionCapacity(SessionDTO sessionDTO, CapacityOperation operation) {
        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(sessionDTO);
        request.setOperation(operation);
        sessionService.changeSessionCapacity(request);
    }

    private void validateBookingRequest(BookingDTO bookingDTO) {
        validateNoDuplicateBooking(bookingDTO);
        validateSessionAvailability(bookingDTO.getSessionDTO());
        if (bookingDTO.getUserSubscriptionDTO() != null) {
            validateUserSubscription(bookingDTO.getUserEmail());
        } else {
            log.info("Booking created without subscription for user {}", bookingDTO.getUserEmail());
        }
    }


    private void validateNoDuplicateBooking(BookingDTO bookingDTO) {
        boolean activeBookingExists = bookingRepository
                .existsByUser_EmailAndSession_Pool_NameAndSession_StartTimeAndStatus(
                        bookingDTO.getUserEmail(),
                        bookingDTO.getSessionDTO().getPoolName(),
                        bookingDTO.getSessionDTO().getStartTime(),
                        BookingStatus.ACTIVE
                );

        if (activeBookingExists) {
            log.warn("Duplicate ACTIVE booking attempt by user {} for session at {}",
                    bookingDTO.getUserEmail(), bookingDTO.getSessionDTO().getStartTime());
            throw new EntityAlreadyExistsException(
                    String.format("User %s already has ACTIVE booking for session at %s in %s",
                            bookingDTO.getUserEmail(),
                            bookingDTO.getSessionDTO().getStartTime(),
                            bookingDTO.getSessionDTO().getPoolName()
                    )
            );
        }
    }

    private void validateUserSubscription(String userEmail) {
        userSubscriptionService.validateUserSubscription(userEmail);
    }

    private void validateSessionAvailability(SessionDTO sessionDTO) {
        sessionService.validateSessionHasAvailableSpots(sessionDTO);
    }

    private Booking createAndPersistBooking(BookingDTO bookingDTO) {
        BookingContext context = bookingContextBuilder.build(
                bookingDTO.getUserEmail(),
                bookingDTO.getSessionDTO()
        );

        Booking booking = new Booking();
        booking.setId(context.getBookingId());
        booking.setUser(context.getUser());
        booking.setSession(context.getSession());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.ACTIVE);

        Optional<UserSubscription> activeSubscription = userSubscriptionService
                .findActiveSubscriptionForUser(bookingDTO.getUserEmail());

        if (activeSubscription.isPresent()) {
            booking.setUserSubscription(activeSubscription.get());
            log.debug("Booking linked to subscription id={}", activeSubscription.get().getId());
        } else {
            log.debug("Booking created without subscription");
        }

        return bookingRepository.save(booking);
    }

    private void updateRelatedEntities(Booking booking) {
        SessionDTO sessionDTO = sessionMapperImpl.toDto(booking.getSession());
        updateSessionCapacity(sessionDTO);

        if (booking.getUserSubscription() != null) {
            userSubscriptionService.decrementRemainingBookings(booking.getUserSubscription().getId());
        }
    }

    private void updateSessionCapacity(SessionDTO sessionDTO) {
        changeSessionCapacity(sessionDTO, CapacityOperation.DECREASE);
    }

    private void publishBookingEvent(BookingDTO bookingDTO) {
        eventPublisher.publishEvent(
                new BookingCreatedEvent(
                        bookingDTO.getUserEmail(),
                        bookingDTO.getSessionDTO()
                )
        );
    }

    private void handleSubscriptionOnDeletion(Booking booking) {
        if (booking.getUserSubscription() != null) {
            userSubscriptionService.incrementRemainingBookings(booking.getUserSubscription().getId());
            log.debug("Returned booking to subscription id={}", booking.getUserSubscription().getId());
        }
    }

    private void handleSubscriptionOnCancellation(Booking booking) {
        if (booking.getUserSubscription() != null) {
            userSubscriptionService.incrementRemainingBookings(booking.getUserSubscription().getId());
            log.debug("Returned booking to subscription id={}", booking.getUserSubscription().getId());
        }
    }

    private void validateBookingCanBeCancelled(Booking booking) {
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            log.warn("Attempt to cancel booking with non-active status: {}, status={}",
                    booking.getId(), booking.getStatus());
            throw new BookingStatusNotActiveException(ErrorMessages.WRONG_STATUS + booking.getStatus());
        }
    }

    private Optional<Booking> findCancelledBookingForSession(BookingDTO bookingDTO) {
        return bookingRepository
                .findByUser_EmailAndSession_Pool_NameAndSession_StartTimeAndStatus(
                        bookingDTO.getUserEmail(),
                        bookingDTO.getSessionDTO().getPoolName(),
                        bookingDTO.getSessionDTO().getStartTime(),
                        BookingStatus.CANCELLED
                )
                .stream()
                .findFirst();
    }
}