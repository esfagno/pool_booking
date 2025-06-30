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
import com.poolapp.pool.service.SessionService;
import com.poolapp.pool.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final PoolRepository poolRepository;
    private final SessionMapper sessionMapper;
    private final PoolService poolService;

    @Override
    public Optional<Session> getSessionByPoolNameAndStartTime(String poolName, LocalDateTime startTime) {
        return sessionRepository.findByPoolNameAndStartTime(poolName, startTime);
    }

    @Override
    public boolean validateSessionHasAvailableSpots(SessionDTO sessionDTO) {
        Session session = getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.SESSION_NOT_FOUND + sessionDTO.getPoolName() + " at time: " + sessionDTO.getStartTime()));

        if (session.getCurrentCapacity() <= 0) {
            throw new NoFreePlacesException(ErrorMessages.NO_FREE_PLACES);
        }
        return true;
    }

    @Override
    public SessionDTO createSession(SessionDTO sessionDTO) {
        Pool pool = poolService.getPoolByName(sessionDTO.getPoolName());

        Session session = sessionMapper.toEntity(sessionDTO);
        session.setPool(pool);

        Session saved = sessionRepository.save(session);
        return sessionMapper.toDto(saved);
    }


    @Override
    public void changeSessionCapacity(SessionDTO sessionDTO, int delta) {
        Session session = getSessionByPoolNameAndStartTime(sessionDTO.getPoolName(), sessionDTO.getStartTime())
                .orElseThrow(() -> new ModelNotFoundException(ErrorMessages.SESSION_NOT_FOUND + sessionDTO.getPoolName() + " at time: " + sessionDTO.getStartTime()));

        int newCapacity = session.getCurrentCapacity() + delta;

        if (newCapacity < 0) {
            throw new NoFreePlacesException(ErrorMessages.NO_FREE_PLACES);
        }

        session.setCurrentCapacity(newCapacity);
        sessionRepository.save(session);
    }


    @Override
    public List<SessionDTO> findSessionsByFilter(SessionDTO sessionDTO) {
        Session session = sessionMapper.toEntity(sessionDTO);
        return sessionRepository.findSessionsByFilter(session).stream()
                .map(sessionMapper::toDto)
                .toList();
    }
}
