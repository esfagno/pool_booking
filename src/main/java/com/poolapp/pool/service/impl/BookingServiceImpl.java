package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.dto.requestDTO.RequestBookingDTO;
import com.poolapp.pool.exception.BookingStatusNotActiveException;
import com.poolapp.pool.exception.EntityAlreadyExistsException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.BookingMapper;
import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.BookingStatus;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.UserSubscriptionRepository;
import com.poolapp.pool.repository.specification.builder.BookingSpecificationBuilder;
import com.poolapp.pool.repository.specification.builder.UserSubscriptionSpecificationBuilder;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final SessionService sessionService;
    private final BookingSpecificationBuilder bookingSpecificationBuilder;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserSubscriptionService userSubscriptionService;
    private final UserSubscriptionSpecificationBuilder userSubscriptionSpecificationBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingContextBuilder bookingContextBuilder;

    @Transactional
    @Override
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        log.debug("Creating booking request for user: {}, session: {}",
                bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        validateBookingRequest(bookingDTO);
        Booking booking = createAndPersistBooking(bookingDTO);
        updateRelatedEntities(bookingDTO);
        publishBookingEvent(bookingDTO);

        log.info("Booking created successfully for user: {}, session: {}",
                bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
        return bookingMapper.toDto(booking);
    }

    @Transactional
    @Override
    public void deleteBooking(BookingDTO bookingDTO) {
        log.debug("Deleting booking for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        Booking booking = findBookingOrThrow(bookingDTO);
        bookingRepository.deleteById(booking.getId());

        modifySubscriptionRemainingBookings(bookingDTO.getUserEmail(), +1);
        changeSessionCapacity(bookingDTO.getSessionDTO(), CapacityOperation.INCREASE);

        log.info("Booking deleted successfully for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
    }

    @Transactional
    @Override
    public void cancelBooking(BookingDTO bookingDTO) {
        log.debug("Cancelling booking for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        Booking booking = findBookingOrThrow(bookingDTO);
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            log.warn("Attempt to cancel booking with non-active status: {}, status={}", bookingDTO, booking.getStatus());
            throw new BookingStatusNotActiveException(ErrorMessages.WRONG_STATUS + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        modifySubscriptionRemainingBookings(bookingDTO.getUserEmail(), +1);
        changeSessionCapacity(bookingDTO.getSessionDTO(), CapacityOperation.INCREASE);

        log.info("Booking cancelled successfully for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
    }

    @Override
    public List<BookingDTO> findBookingsByFilter(RequestBookingDTO filterDTO) {
        log.debug("Finding bookings by filter: {}", filterDTO);

        Booking filter = bookingMapper.toEntity(filterDTO);
        try {
            BookingContext context = bookingContextBuilder.build(filterDTO.getUserEmail(), filterDTO.getRequestSessionDTO());
            filter.setId(context.getBookingId());
        } catch (ModelNotFoundException ignored) {
        }

        Specification<Booking> spec = bookingSpecificationBuilder.buildSpecification(filter);
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
        Session session = sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime()).orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, "pool=" + sessionDTO.getPoolName() + ", startTime=" + sessionDTO.getStartTime()));
        bookingRepository.deleteAllBySessionId(session.getId());
        log.info("Bookings deleted for session id={}", session.getId());
    }

    @Override
    public Long countBookingsBySession(SessionDTO sessionDTO) {
        log.debug("Counting bookings by session: {}", sessionDTO);
        Session session = sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime()).orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, "pool=" + sessionDTO.getPoolName() + ", startTime=" + sessionDTO.getStartTime()));
        long count = bookingRepository.countBySessionId(session.getId());
        log.debug("Counted {} bookings for session id={}", count, session.getId());
        return count;
    }

    private Booking findBookingOrThrow(BookingDTO bookingDTO) {
        BookingContext context = bookingContextBuilder.build(bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
        return bookingRepository.findById(context.getBookingId()).orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, String.format("Booking not found: user=%s, startTime=%s", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO().getStartTime())));
    }

    private void modifySubscriptionRemainingBookings(String userEmail, int delta) {
        Specification<UserSubscription> spec = userSubscriptionSpecificationBuilder.activeWithRemainingBookingsAndUserEmail(userEmail, 0);

        userSubscriptionRepository.findOne(spec).ifPresent(sub -> {
            int newCount = sub.getRemainingBookings() + delta;
            if (newCount < 0) {
                log.error("Attempt to set negative remaining bookings for user {}", userEmail);
                return;
            }

            sub.setRemainingBookings(newCount);
            userSubscriptionRepository.save(sub);
            log.debug("Updated remaining bookings for subscription id={} to {}", sub.getId(), newCount);
        });
    }

    private void changeSessionCapacity(SessionDTO sessionDTO, CapacityOperation operation) {
        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(sessionDTO);
        request.setOperation(operation);
        sessionService.changeSessionCapacity(request);
    }

    private void validateBookingRequest(BookingDTO bookingDTO) {
        validateNoDuplicateBooking(bookingDTO);
        validateUserSubscription(bookingDTO.getUserEmail());
        validateSessionAvailability(bookingDTO.getSessionDTO());
    }

    private void validateNoDuplicateBooking(BookingDTO bookingDTO) {
        if (hasUserBookedForSession(
                bookingDTO.getUserEmail(),
                bookingDTO.getSessionDTO().getPoolName(),
                bookingDTO.getSessionDTO().getStartTime()
        )) {
            log.warn("Duplicate booking attempt by user {} for session at {}",
                    bookingDTO.getUserEmail(), bookingDTO.getSessionDTO().getStartTime());
            throw new EntityAlreadyExistsException(
                    String.format("User %s already booked session at %s in %s",
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

        return bookingRepository.save(booking);
    }

    private void updateRelatedEntities(BookingDTO bookingDTO) {
        updateSubscriptionRemainingBookings(bookingDTO.getUserEmail());
        updateSessionCapacity(bookingDTO.getSessionDTO());
    }

    private void updateSubscriptionRemainingBookings(String userEmail) {
        modifySubscriptionRemainingBookings(userEmail, -1);
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
}
