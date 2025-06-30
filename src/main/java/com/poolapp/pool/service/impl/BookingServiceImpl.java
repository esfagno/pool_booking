package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.mapper.BookingMapper;
import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.BookingId;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.enums.BookingStatus;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.SessionRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.service.BookingService;
import com.poolapp.pool.service.MailService;
import com.poolapp.pool.service.SessionService;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BookingMapper bookingMapper;
    private final PoolRepository poolRepository;
    private final UserService userService;
    private final SessionService sessionService;
    private final MailService mailService;


    private Booking findBooking(BookingDTO bookingDTO) {
        return bookingRepository.findById(buildBookingId(bookingDTO))
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.BOOKING_NOT_FOUND));
    }

    private BookingId buildBookingId(BookingDTO bookingDTO) {
        User user = userService.findUserByEmail(bookingDTO.getUserEmail());
        Session session = sessionService.getSessionByPoolNameAndStartTime(bookingDTO.getSessionDTO().getPoolName(), bookingDTO.getSessionDTO().getStartTime());
        return new BookingId(user.getId(), session.getId());
    }

    @Transactional
    @Override
    public BookingDTO createBooking(BookingDTO bookingDTO) {

//I will add the subscription implementation later and add and I’ll add the subscription logic here

        BookingId bookingId = buildBookingId(bookingDTO);
        userService.hasActiveBooking(bookingDTO.getUserEmail(), LocalDateTime.now());
        sessionService.validateSessionHasAvailableSpots(bookingDTO.getSessionDTO());

        Booking booking = bookingMapper.toEntity(bookingDTO);
        booking.setId(bookingId);

        Booking saved = bookingRepository.save(booking);
        sessionService.decrementSessionCapacity(bookingDTO.getSessionDTO());
        mailService.sendBookingConfirmationEmail(bookingDTO.getUserEmail(), bookingDTO.getSessionDTO());

        return bookingMapper.toDto(saved);
    }

    @Transactional
    @Override
    public void deleteBooking(BookingDTO bookingDTO) {
        bookingRepository.deleteById(buildBookingId(bookingDTO));
        sessionService.incrementSessionCapacity(bookingDTO.getSessionDTO());
    }

    @Transactional
    @Override
    public void cancelBooking(BookingDTO bookingDTO) {
        if (findBooking(bookingDTO).getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException(ErrorMessages.WRONG_STATUS + findBooking(bookingDTO).getStatus());
        }
        findBooking(bookingDTO).setStatus(BookingStatus.CANCELLED);
        sessionService.incrementSessionCapacity(bookingDTO.getSessionDTO());
    }

    @Override
    public List<BookingDTO> findBookingsByFilter(BookingDTO bookingDTO) {
        BookingId bookingId = buildBookingId(bookingDTO);
        Booking booking = bookingMapper.toEntity(bookingDTO);
        booking.setId(bookingId);
        List<Booking> bookings = bookingRepository.findBookingsByFilter(booking);
        return bookingMapper.toDtoList(bookings);
    }

    @Override
    public BookingDTO updateBooking(BookingDTO bookingDTO, BookingDTO newBookingDTO) {
        Booking booking = findBooking(bookingDTO);
        bookingMapper.updateBookingFromDto(booking, newBookingDTO);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public boolean hasUserBooked(String userEmail, LocalDateTime sessionStartTime) {
        return !bookingRepository.findByUser_EmailAndSession_StartTime(userEmail, sessionStartTime).isEmpty();
    }

    @Override
    @Transactional
    public void expirePastBookings(LocalDateTime now) {
        List<Booking> expired = bookingRepository.findBySession_StartTimeBeforeAndStatus(now, BookingStatus.ACTIVE);
        expired.forEach(b -> b.setStatus(BookingStatus.COMPLETED));
        bookingRepository.saveAll(expired);
    }

    @Override
    public void deleteBookingsBySession(SessionDTO sessionDTO) {
        Session session = sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
        bookingRepository.deleteAllBySessionId(session.getId());

    }

    @Override
    public long countBookingsBySession(SessionDTO sessionDTO) {
        Session session = sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
        return bookingRepository.countBySessionId(session.getId());
    }

}
