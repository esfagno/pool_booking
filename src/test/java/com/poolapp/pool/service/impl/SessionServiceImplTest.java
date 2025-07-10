package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.PoolDTO;
import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.NoFreePlacesException;
import com.poolapp.pool.mapper.PoolMapper;
import com.poolapp.pool.mapper.SessionMapper;
import com.poolapp.pool.model.Pool;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.SessionRepository;
import com.poolapp.pool.service.PoolService;
import com.poolapp.pool.util.CapacityOperation;
import com.poolapp.pool.util.ChangeSessionCapacityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private PoolRepository poolRepository;

    @MockBean
    private PoolService poolService;

    @Autowired
    private SessionMapper sessionMapper = Mappers.getMapper(SessionMapper.class);

    @Autowired
    private PoolMapper poolMapper = Mappers.getMapper(PoolMapper.class);

    @Autowired
    private SessionServiceImpl sessionService;

    private Pool pool;
    private Session session;
    private SessionDTO sessionDTO;
    private PoolDTO poolDTO;

    @BeforeEach
    void setUp() {

        pool = new Pool();
        pool.setId(1);
        pool.setName("Main Pool");
        pool.setMaxCapacity(20);

        poolDTO = new PoolDTO();
        poolDTO.setName("Main Pool");

        session = new Session();
        session.setId(1);
        session.setPool(pool);
        session.setStartTime(LocalDateTime.of(2025, 6, 27, 10, 0));
        session.setCurrentCapacity(5);

        sessionDTO = new SessionDTO();
        sessionDTO.setPoolDTO(poolDTO);
        sessionDTO.setStartTime(LocalDateTime.of(2025, 6, 27, 10, 0));


    }

    @Test
    void test_createSession_shouldSaveAndReturnSessionDTO() {
        when(poolService.getPoolByName("Main Pool")).thenReturn(pool);
        when(sessionRepository.save(any())).thenReturn(session);

        SessionDTO result = sessionService.createSession(sessionDTO);

        assertNotNull(result);
        assertEquals("Main Pool", result.getPoolDTO().getName());
        verify(sessionRepository, times(1)).save(any());
    }

    @Test
    void test_changeSessionCapacity_shouldDecreaseCapacity() {
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));

        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(sessionDTO);
        request.setOperation(CapacityOperation.DECREASE);

        sessionService.changeSessionCapacity(request);

        assertEquals(4, session.getCurrentCapacity());
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    void test_changeSessionCapacity_shouldThrowIfNoFreePlaces() {
        session.setCurrentCapacity(0);
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));

        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(sessionDTO);
        request.setOperation(CapacityOperation.DECREASE);

        assertThrows(NoFreePlacesException.class, () -> sessionService.changeSessionCapacity(request));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void test_changeSessionCapacity_shouldThrowIfSessionNotFound() {
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.empty());

        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(sessionDTO);
        request.setOperation(CapacityOperation.DECREASE);


        assertThrows(ModelNotFoundException.class, () -> sessionService.changeSessionCapacity(request));
    }

    @Test
    void test_changeSessionCapacity_shouldIncreaseCapacity() {
        when(sessionRepository.findByPoolNameAndStartTime("Main Pool", sessionDTO.getStartTime()))
                .thenReturn(Optional.of(session));


        ChangeSessionCapacityRequest request = new ChangeSessionCapacityRequest();
        request.setSessionDTO(sessionDTO);
        request.setOperation(CapacityOperation.INCREASE);

        sessionService.changeSessionCapacity(request);
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

        when(sessionRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(sessionResult));

        List<SessionDTO> result = sessionService.findSessionsByFilter(sessionDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        SessionDTO dto = result.get(0);
        assertEquals("Main Pool", dto.getPoolDTO().getName());
        assertEquals(LocalDateTime.of(2025, 6, 27, 12, 0), dto.getStartTime());

        verify(sessionRepository, times(1))
                .findAll(any(Specification.class));
    }
}
