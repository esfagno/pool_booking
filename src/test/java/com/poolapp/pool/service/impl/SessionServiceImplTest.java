package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.NoFreePlacesException;
import com.poolapp.pool.mapper.SessionMapper;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.SessionRepository;
import com.poolapp.pool.service.PoolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private PoolService poolService;

    @Spy
    private SessionMapper sessionMapper = Mappers.getMapper(SessionMapper.class);

    @InjectMocks
    private SessionServiceImpl sessionService;

    private Pool pool;
    private Session session;
    private SessionDTO sessionDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        pool = new Pool();
        pool.setId(1);
        pool.setName("Main Pool");
        pool.setMaxCapacity(20);

        session = new Session();
        session.setId(1);
        session.setPool(pool);
        session.setStartTime(LocalDateTime.of(2025, 6, 27, 10, 0));
        session.setCurrentCapacity(5);

        sessionDTO = new SessionDTO();
        sessionDTO.setPoolName("Main Pool");
        sessionDTO.setStartTime(LocalDateTime.of(2025, 6, 27, 10, 0));
    }

    @Test
    void test_createSession_shouldSaveAndReturnSessionDTO() {
        when(poolService.getPoolByName("Main Pool")).thenReturn(pool);
        when(sessionRepository.save(any())).thenReturn(session);

        SessionDTO result = sessionService.createSession(sessionDTO);

        assertNotNull(result);
        assertEquals("Main Pool", result.getPoolName());
        verify(poolService, times(1)).getPoolByName("Main Pool");
        verify(sessionRepository, times(1)).save(any());
    }

    @Test
    void test_changeSessionCapacity_shouldDecreaseCapacity() {
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));

        sessionService.changeSessionCapacity(sessionDTO, -1);

        assertEquals(4, session.getCurrentCapacity());
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    void test_changeSessionCapacity_shouldThrowIfNoFreePlaces() {
        session.setCurrentCapacity(0);
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));

        assertThrows(NoFreePlacesException.class, () -> sessionService.changeSessionCapacity(sessionDTO, -1));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void test_changeSessionCapacity_shouldThrowIfSessionNotFound() {
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.empty());

        assertThrows(ModelNotFoundException.class, () -> sessionService.changeSessionCapacity(sessionDTO, -1));
    }

    @Test
    void test_changeSessionCapacity_shouldIncreaseCapacity() {
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));

        sessionService.changeSessionCapacity(sessionDTO, 1);

        assertEquals(6, session.getCurrentCapacity());
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    void test_getSessionByPoolNameAndStartTime_shouldReturnSession() {
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));

        Optional<Session> result = sessionService.getSessionByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime());

        assertTrue(result.isPresent());
        assertEquals(session, result.get());
    }

    @Test
    void test_validateSessionHasAvailableSpots_shouldReturnTrueIfAvailable() {
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));

        boolean result = sessionService.validateSessionHasAvailableSpots(sessionDTO);

        assertTrue(result);
    }

    @Test
    void test_validateSessionHasAvailableSpots_shouldThrowIfNoSpots() {
        session.setCurrentCapacity(0);
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));

        assertThrows(NoFreePlacesException.class, () -> sessionService.validateSessionHasAvailableSpots(sessionDTO));
    }

    @Test
    void test_findSessionsByFilter_shouldReturnListOfSessionDTO() {
        Session sessionResult = new Session();
        sessionResult.setId(2);
        sessionResult.setStartTime(LocalDateTime.of(2025, 6, 27, 12, 0));
        Pool pool = new Pool();
        pool.setId(1);
        pool.setName("Main Pool");
        sessionResult.setPool(pool);
        sessionResult.setCurrentCapacity(10);

        when(sessionRepository.findSessionsByFilter(eq("Main Pool"), any(), any()))
                .thenReturn(List.of(sessionResult));

        List<SessionDTO> result = sessionService.findSessionsByFilter(sessionDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        SessionDTO dto = result.get(0);
        assertEquals("Main Pool", dto.getPoolName());
        assertEquals(LocalDateTime.of(2025, 6, 27, 12, 0), dto.getStartTime());

        verify(sessionRepository, times(1))
                .findSessionsByFilter(eq("Main Pool"), any(), any());
    }
}
