package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.NoFreePlacesException;
import com.poolapp.pool.exception.NotActiveException;
import com.poolapp.pool.mapper.BookingMapper;
import com.poolapp.pool.mapper.SessionMapper;
import com.poolapp.pool.model.Booking;
import com.poolapp.pool.model.BookingId;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.model.User;
import com.poolapp.pool.model.enums.BookingStatus;
import com.poolapp.pool.repository.BookingRepository;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.SessionRepository;
import com.poolapp.pool.repository.UserRepository;
import com.poolapp.pool.service.MailService;
import com.poolapp.pool.service.SessionService;
import com.poolapp.pool.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.poolapp.pool.model.enums.BookingStatus.ACTIVE;
import static com.poolapp.pool.model.enums.BookingStatus.CANCELLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookingServiceImplTest {

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private PoolRepository poolRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private MailService mailService;

    @SpyBean
    private BookingMapper bookingMapper;

    @SpyBean
    private SessionMapper sessionMapper;


    @Autowired
    private BookingServiceImpl bookingService;

    private BookingDTO bookingDTO;
    private SessionDTO sessionDTO;
    private User user;
    private Session session;
    private Booking booking;
    private Pool pool;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1);
        user.setEmail("user@example.com");

        pool = new Pool();
        pool.setName("Main Pool");

        session = new Session();
        session.setId(10);
        session.setCurrentCapacity(5);
        session.setStartTime(LocalDateTime.of(2025, 6, 30, 18, 0));
        session.setPool(pool);

        bookingDTO = new BookingDTO();
        bookingDTO.setUserEmail("user@example.com");
        bookingDTO.setSessionDTO(sessionDTO);

        sessionDTO = new SessionDTO();
        sessionDTO.setPoolName("Main Pool");
        sessionDTO.setStartTime(LocalDateTime.of(2025, 6, 30, 18, 0));

        bookingDTO.setSessionDTO(sessionDTO);

        booking = new Booking();
        booking.setStatus(ACTIVE);
        booking.setSession(session);
        booking.setUser(user);

        booking.setSession(session);
    }


    @Test
    void test_createBooking_success() {
        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(userService.hasActiveBooking(eq(bookingDTO.getUserEmail()), any(LocalDateTime.class))).thenReturn(false);
        when(sessionService.validateSessionHasAvailableSpots(sessionDTO)).thenReturn(true);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDTO result = bookingService.createBooking(bookingDTO);

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any());
        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verify(userService, times(1)).hasActiveBooking(eq(bookingDTO.getUserEmail()), any(LocalDateTime.class));
        verify(sessionService, times(1)).getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
        verify(sessionService, times(1)).validateSessionHasAvailableSpots(sessionDTO);
    }


    @Test
    void test_createBooking_userNotFound_shouldThrowException() {
        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> bookingService.createBooking(bookingDTO));

        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verifyNoInteractions(sessionService);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void test_createBooking_sessionNotFound_shouldThrowException() {
        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> bookingService.createBooking(bookingDTO));

        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verify(sessionService, times(1)).getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
        verifyNoMoreInteractions(sessionService);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void test_createBooking_hasActiveBooking_shouldThrowException() {

        when(bookingRepository.existsByUserIdAndSessionStartTimeAfter(anyInt(), any())).thenReturn(true);

        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(userService.hasActiveBooking(eq(bookingDTO.getUserEmail()), any(LocalDateTime.class))).thenReturn(true);

        assertThrows(NotActiveException.class, () -> bookingService.createBooking(bookingDTO));

        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verify(sessionService, times(1)).getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
        verify(userService, times(1)).hasActiveBooking(eq(bookingDTO.getUserEmail()), any(LocalDateTime.class));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void test_createBooking_noAvailableSpots_shouldThrowException() {
        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(userService.hasActiveBooking(eq(bookingDTO.getUserEmail()), any(LocalDateTime.class))).thenReturn(false);
        doThrow(new NoFreePlacesException("No available spots")).when(sessionService).validateSessionHasAvailableSpots(sessionDTO);

        assertThrows(NoFreePlacesException.class, () -> bookingService.createBooking(bookingDTO));

        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verify(sessionService, times(1)).getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
        verify(userService, times(1)).hasActiveBooking(eq(bookingDTO.getUserEmail()), any(LocalDateTime.class));
        verify(sessionService, times(1)).validateSessionHasAvailableSpots(sessionDTO);
        verify(bookingRepository, never()).save(any());
    }


    @Test
    void test_deleteBooking_success() {
        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));

        bookingService.deleteBooking(bookingDTO);

        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verify(sessionService, times(1)).getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
        verify(bookingRepository, times(1)).deleteById(new BookingId(user.getId(), session.getId()));
    }

    @Test
    void test_deleteBooking_userNotFound_shouldThrowException() {
        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> bookingService.deleteBooking(bookingDTO));

        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verifyNoInteractions(sessionService);
        verify(bookingRepository, never()).deleteById(any());
    }

    @Test
    void test_deleteBooking_sessionNotFound_shouldThrowException() {
        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> bookingService.deleteBooking(bookingDTO));

        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verify(sessionService, times(1)).getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
        verify(bookingRepository, never()).deleteById(any());
    }

    @Test
    void test_cancelBooking_shouldSetStatusToCancelled_whenStatusIsActive() {
        Booking booking = new Booking();
        booking.setStatus(ACTIVE);

        BookingId bookingId = new BookingId(user.getId(), session.getId());

        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(bookingDTO);

        verify(sessionService, times(1)).changeSessionCapacity(sessionDTO, 1);
        assertEquals(CANCELLED, booking.getStatus());

    }

    @Test
    void test_cancelBooking_shouldThrowException_whenStatusIsNotActive() {
        Booking booking = new Booking();
        booking.setStatus(CANCELLED);

        BookingId bookingId = new BookingId(user.getId(), session.getId());

        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(NotActiveException.class, () -> bookingService.cancelBooking(bookingDTO));

        verify(sessionService, never()).changeSessionCapacity(any(), anyInt());
    }


    @Test
    void test_findBookingsByFilter_successByDTO() {
        Booking booking = new Booking();
        booking.setId(new BookingId(user.getId(), session.getId()));
        booking.setUser(user);
        booking.setSession(session);
        booking.setStatus(BookingStatus.ACTIVE);

        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn((Optional.of(session)));
        when(bookingRepository.findBookingsByFilter(any(BookingId.class), any(BookingStatus.class), anyString(), any(LocalDateTime.class))).thenReturn(List.of(booking));
        List<BookingDTO> result = bookingService.findBookingsByFilter(bookingDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingDTO.getUserEmail(), result.get(0).getUserEmail());
        verify(bookingRepository, times(1)).findBookingsByFilter(any(BookingId.class), any(BookingStatus.class), anyString(), any(LocalDateTime.class));
    }

    @Test
    void test_findBookingsByFilter_multipleFieldsByDTO() {
        Booking booking1 = new Booking();
        booking1.setStatus(BookingStatus.ACTIVE);
        booking1.setUser(user);
        booking1.setSession(session);

        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(bookingRepository.findBookingsByFilter(any(BookingId.class), any(BookingStatus.class), anyString(), any(LocalDateTime.class))).thenReturn(List.of(booking));
        List<BookingDTO> result = bookingService.findBookingsByFilter(bookingDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BookingStatus.ACTIVE, result.get(0).getStatus());

        verify(bookingRepository, times(1)).findBookingsByFilter(any(BookingId.class), any(BookingStatus.class), anyString(), any(LocalDateTime.class));
        verify(userService, times(1)).findUserByEmail(bookingDTO.getUserEmail());
        verify(sessionService, times(1)).getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime());
    }

    @Test
    void test_findBookingsByFilter_noBookingsFound_shouldReturnEmptyListByDTO() {
        when(userService.findUserByEmail(bookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(bookingRepository.findBookingsByFilter(any(BookingId.class), any(BookingStatus.class), anyString(), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        List<BookingDTO> result = bookingService.findBookingsByFilter(bookingDTO);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(bookingRepository, times(1)).findBookingsByFilter(any(BookingId.class), any(BookingStatus.class), anyString(), any(LocalDateTime.class));
    }

    //
    @Test
    void test_updateBooking_shouldUpdateFieldsAndSave() {
        BookingDTO newBookingDTO = new BookingDTO();
        newBookingDTO.setUserEmail(bookingDTO.getUserEmail());
        SessionDTO newSessionDTO = new SessionDTO();
        newSessionDTO.setPoolName("Updated Pool");
        newSessionDTO.setStartTime(LocalDateTime.of(2025, 7, 1, 18, 0));
        newBookingDTO.setSessionDTO(newSessionDTO);

        BookingId bookingId = new BookingId(user.getId(), session.getId());

        when(userService.findUserByEmail(newBookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(newSessionDTO.getPoolName(), newSessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionService.getSessionByPoolNameAndStartTime(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(session));

        BookingDTO updated = bookingService.updateBooking(bookingDTO, newBookingDTO);

        assertNotNull(updated);
        verify(bookingMapper, times(1)).updateBookingFromDto(booking, newBookingDTO);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void test_updateBooking_shouldThrowException_whenBookingNotFound() {
        BookingDTO newBookingDTO = new BookingDTO();
        newBookingDTO.setUserEmail(bookingDTO.getUserEmail());
        newBookingDTO.setSessionDTO(sessionDTO);

        BookingId bookingId = new BookingId(user.getId(), session.getId());

        when(userService.findUserByEmail(newBookingDTO.getUserEmail())).thenReturn(Optional.of(user));
        when(sessionService.getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())).thenReturn(Optional.of(session));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> bookingService.updateBooking(bookingDTO, newBookingDTO));

        verify(bookingMapper, never()).updateBookingFromDto(any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void test_hasUserBooked_trueWhenExists() {
        when(bookingRepository.findByUser_EmailAndSession_StartTime(eq(bookingDTO.getUserEmail()), eq(session.getStartTime())))
                .thenReturn(List.of(new Booking()));

        boolean result = bookingService.hasUserBooked(bookingDTO.getUserEmail(), session.getStartTime());
        assertTrue(result);
    }

    @Test
    void test_hasUserBooked_falseWhenEmpty() {
        when(bookingRepository.findByUser_EmailAndSession_StartTime(eq(bookingDTO.getUserEmail()), eq(session.getStartTime())))
                .thenReturn(List.of());

        boolean result = bookingService.hasUserBooked(bookingDTO.getUserEmail(), session.getStartTime());
        assertFalse(result);
    }

    @Test
    void test_expirePastBookings_changesStatusAndSaves() {
        Booking b1 = new Booking();
        b1.setStatus(BookingStatus.ACTIVE);
        Booking b2 = new Booking();
        b2.setStatus(BookingStatus.ACTIVE);

        when(bookingRepository.findBySession_StartTimeBeforeAndStatus(any(LocalDateTime.class), eq(BookingStatus.ACTIVE)))
                .thenReturn(List.of(b1, b2));

        bookingService.expirePastBookings(LocalDateTime.now());

        assertEquals(BookingStatus.COMPLETED, b1.getStatus());
        assertEquals(BookingStatus.COMPLETED, b2.getStatus());
        verify(bookingRepository, times(1)).saveAll(List.of(b1, b2));
    }

    @Test
    void test_deleteBookingsBySession_invokesRepository() {
        SessionDTO dto = sessionDTO;
        Session mockSession = new Session();
        mockSession.setId(99);

        when(sessionService.getSessionByPoolNameAndStartTime(dto.getPoolName(), dto.getStartTime())).thenReturn(Optional.of(mockSession));

        bookingService.deleteBookingsBySession(dto);

        verify(bookingRepository, times(1)).deleteAllBySessionId(99);
    }

    @Test
    void test_countBookingsBySession_returnsCount() {
        SessionDTO dto = sessionDTO;
        Session mockSession = new Session();
        mockSession.setId(123);

        when(sessionService.getSessionByPoolNameAndStartTime(dto.getPoolName(), dto.getStartTime())).thenReturn(Optional.of(mockSession));
        when(bookingRepository.countBySessionId(123)).thenReturn(7L);

        long count = bookingService.countBookingsBySession(dto);

        assertEquals(7L, count);
    }
}
