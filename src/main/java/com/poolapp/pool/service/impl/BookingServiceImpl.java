package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.exception.BookingStatusNotActiveException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.BookingMapper;
import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.BookingId;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.UserSubscription;
import com.poolapp.pool.model.enums.BookingStatus;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.UserSubscriptionRepository;
import com.poolapp.pool.repository.specification.builder.BookingSpecificationBuilder;
import com.poolapp.pool.repository.specification.builder.UserSubscriptionSpecificationBuilder;
import com.poolapp.pool.service.BookingService;
import com.poolapp.pool.service.MailService;
import com.poolapp.pool.service.SessionService;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.service.UserSubscriptionService;
import com.poolapp.pool.util.CapacityOperation;
import com.poolapp.pool.util.ChangeSessionCapacityRequest;
import com.poolapp.pool.util.exception.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MailService mailService;
    private final BookingSpecificationBuilder bookingSpecificationBuilder;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserSubscriptionService userSubscriptionService;
    private final UserSubscriptionSpecificationBuilder userSubscriptionSpecificationBuilder;

    @Transactional
    @Override
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        log.debug("Creating booking for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
        BookingId bookingId = buildBookingId(bookingDTO);

        Optional<UserSubscription> maybeSubscription = userSubscriptionService.validateUserSubscription(bookingDTO.getUserEmail());

        sessionService.validateSessionHasAvailableSpots(bookingDTO.getSessionDTO());

        Booking booking = bookingMapper.toEntity(bookingDTO);
        booking.setId(bookingId);
        Booking saved = bookingRepository.save(booking);

        maybeSubscription.ifPresent(subscription -> {
            subscription.setRemainingBookings(subscription.getRemainingBookings() - 1);
            userSubscriptionRepository.save(subscription);
            log.debug("Decreased remaining bookings for subscription id={}", subscription.getId());
        });

        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(bookingDTO.getSessionDTO());
        request.setOperation(CapacityOperation.DECREASE);
        sessionService.changeSessionCapacity(request);

        mailService.sendBookingConfirmationEmail(bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
        log.info("Booking created successfully for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        return bookingMapper.toDto(saved);
    }

    @Transactional
    @Override
    public void deleteBooking(BookingDTO bookingDTO) {
        log.debug("Deleting booking for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
        BookingId bookingId = buildBookingId(bookingDTO);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.warn("Booking not found for deletion: user={}, session={}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
            return new ModelNotFoundException(ErrorMessages.BOOKING_NOT_FOUND);
        });
        bookingRepository.deleteById(bookingId);

        Specification<UserSubscription> spec = userSubscriptionSpecificationBuilder.activeWithRemainingBookingsAndUserEmail(bookingDTO.getUserEmail(), 0);

        userSubscriptionRepository.findOne(spec).ifPresent(userSubscription -> {
            userSubscription.setRemainingBookings(userSubscription.getRemainingBookings() + 1);
            userSubscriptionRepository.save(userSubscription);
            log.debug("Increased remaining bookings for subscription id={}", userSubscription.getId());
        });

        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(bookingDTO.getSessionDTO());
        request.setOperation(CapacityOperation.INCREASE);
        sessionService.changeSessionCapacity(request);
        log.info("Booking deleted successfully for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
    }

    @Transactional
    @Override
    public void cancelBooking(BookingDTO bookingDTO) {
        log.debug("Cancelling booking for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
        Booking booking = findBookingByDTO(bookingDTO);

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            log.warn("Attempt to cancel booking with non-active status: {}, status={}", bookingDTO, booking.getStatus());
            throw new BookingStatusNotActiveException(ErrorMessages.WRONG_STATUS + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);

        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(bookingDTO.getSessionDTO());
        request.setOperation(CapacityOperation.INCREASE);
        sessionService.changeSessionCapacity(request);

        Specification<UserSubscription> spec = userSubscriptionSpecificationBuilder.activeWithRemainingBookingsAndUserEmail(bookingDTO.getUserEmail(), 0);

        userSubscriptionRepository.findOne(spec).ifPresent(userSubscription -> {
            userSubscription.setRemainingBookings(userSubscription.getRemainingBookings() + 1);
            userSubscriptionRepository.save(userSubscription);
            log.debug("Increased remaining bookings for subscription id={}", userSubscription.getId());
        });

        bookingRepository.save(booking);
        log.info("Booking cancelled successfully for user: {}, session: {}", bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());
    }


    @Override
    public List<BookingDTO> findBookingsByFilter(BookingDTO filterDTO) {
        log.debug("Finding bookings by filter: {}", filterDTO);
        BookingId bookingId = buildBookingId(filterDTO);
        Booking filter = bookingMapper.toEntity(filterDTO);
        filter.setId(bookingId);
        Specification<Booking> spec = bookingSpecificationBuilder.buildSpecification(filter);
        List<Booking> bookings = bookingRepository.findAll(spec);
        log.debug("Found {} bookings with given filter", bookings.size());
        return bookingMapper.toDtoList(bookings);
    }

    @Override
    public BookingDTO updateBooking(BookingDTO bookingDTO, BookingDTO newBookingDTO) {
        log.debug("Updating booking: {}", bookingDTO);
        Booking booking = findBookingByDTO(bookingDTO);
        bookingMapper.updateBookingFromDto(booking, newBookingDTO);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking updated successfully: id={}", savedBooking.getId());
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public boolean hasUserBooked(String userEmail, LocalDateTime sessionStartTime) {
        log.debug("Checking if user has booking: user={}, sessionStartTime={}", userEmail, sessionStartTime);
        boolean result = !bookingRepository.findByUser_EmailAndSession_StartTime(userEmail, sessionStartTime).isEmpty();
        log.info("User {} has booking: {}", userEmail, result);
        return result;
    }

    @Override
    @Transactional
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
        Session session = sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime()).orElseThrow(() -> {
            log.warn("Session not found for deleting bookings: pool={}, startTime={}", sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime());
            return new ModelNotFoundException(String.format(ErrorMessages.SESSION_NOT_FOUND, sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime()));
        });
        bookingRepository.deleteAllBySessionId(session.getId());
        log.info("Bookings deleted for session id={}", session.getId());
    }

    @Override
    public Long countBookingsBySession(SessionDTO sessionDTO) {
        log.debug("Counting bookings by session: {}", sessionDTO);
        Session session = sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime()).orElseThrow(() -> {
            log.warn("Session not found for counting bookings: pool={}, startTime={}", sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime());
            return new ModelNotFoundException(String.format(ErrorMessages.SESSION_NOT_FOUND, sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime()));
        });
        long count = bookingRepository.countBySessionId(session.getId());
        log.debug("Counted {} bookings for session id={}", count, session.getId());
        return count;
    }

    private Booking findBookingByDTO(BookingDTO bookingDTO) {
        log.debug("Finding booking by DTO: {}", bookingDTO);
        return bookingRepository.findById(buildBookingId(bookingDTO)).orElseThrow(() -> {
            log.warn("Booking not found: {}", bookingDTO);
            return new ModelNotFoundException(ErrorMessages.BOOKING_NOT_FOUND);
        });
    }

    private BookingId buildBookingId(BookingDTO bookingDTO) {
        User user = userService.findUserByEmail(bookingDTO.getUserEmail()).orElseThrow(() -> {
            log.warn("User not found while building BookingId: {}", bookingDTO.getUserEmail());
            return new ModelNotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, bookingDTO.getUserEmail()));
        });

        Session session = sessionService.getSessionByPoolNameAndStartTime(bookingDTO.getSessionDTO().getPoolDTO().getName(), bookingDTO.getSessionDTO().getStartTime()).orElseThrow(() -> {
            log.warn("Session not found while building BookingId: pool={}, startTime={}", bookingDTO.getSessionDTO().getPoolDTO().getName(), bookingDTO.getSessionDTO().getStartTime());
            return new ModelNotFoundException(String.format(ErrorMessages.SESSION_NOT_FOUND, bookingDTO.getSessionDTO().getPoolDTO().getName(), bookingDTO.getSessionDTO().getStartTime()));
        });
        return new BookingId(user.getId(), session.getId());
    }
}
