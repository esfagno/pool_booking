package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.NoFreePlacesException;
import com.poolapp.pool.mapper.SessionMapper;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.repository.PoolRepository;
import com.poolapp.pool.repository.SessionRepository;
import com.poolapp.pool.repository.specification.builder.SessionSpecificationBuilder;
import com.poolapp.pool.service.PoolService;
import com.poolapp.pool.service.SessionService;
import com.poolapp.pool.util.ChangeSessionCapacityRequest;
import com.poolapp.pool.util.exception.ApiErrorCode;
import com.poolapp.pool.util.exception.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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
    private final SessionSpecificationBuilder sessionSpecificationBuilder;

    @Override
    public Optional<Session> getSessionByPoolNameAndStartTime(String poolName, LocalDateTime startTime) {
        return sessionRepository.findByPoolNameAndStartTime(poolName, startTime);
    }

    @Override
    public boolean validateSessionHasAvailableSpots(SessionDTO sessionDTO) {
        Session session = getSessionByPoolNameAndStartTime(sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime()).orElseThrow(() -> {
            String details = String.format("pool=%s, startTime=%s", sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime());

            return new ModelNotFoundException(ApiErrorCode.NOT_FOUND, details);
        });

        if (session.getCurrentCapacity() <= 0) {
            throw new NoFreePlacesException(ErrorMessages.NO_FREE_PLACES);
        }
        return true;
    }

    @Override
    public SessionDTO createSession(SessionDTO sessionDTO) {
        Session session = sessionMapper.toEntity(sessionDTO);
        Session saved = sessionRepository.save(session);
        return sessionMapper.toDto(saved);
    }


    @Override
    public void changeSessionCapacity(ChangeSessionCapacityRequest request) {
        SessionDTO sessionDTO = request.getSessionDTO();
        Session session = getSessionByPoolNameAndStartTime(sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime()).orElseThrow(() -> {
            String details = String.format("pool=%s, startTime=%s", sessionDTO.getPoolDTO().getName(), sessionDTO.getStartTime());

            return new ModelNotFoundException(ApiErrorCode.NOT_FOUND, details);
        });
        int newCapacity = switch (request.getOperation()) {
            case INCREASE -> session.getCurrentCapacity() + 1;
            case DECREASE -> session.getCurrentCapacity() - 1;
        };

        if (newCapacity < 0) {
            throw new NoFreePlacesException(ErrorMessages.NO_FREE_PLACES);
        }

        session.setCurrentCapacity(newCapacity);
        sessionRepository.save(session);
    }


    @Override
    public List<SessionDTO> findSessionsByFilter(SessionDTO filterDTO) {
        Session filter = sessionMapper.toEntity(filterDTO);
        Specification<Session> spec = sessionSpecificationBuilder.buildSpecification(filter);
        return sessionRepository.findAll(spec).stream().map(sessionMapper::toDto).toList();
    }
}
