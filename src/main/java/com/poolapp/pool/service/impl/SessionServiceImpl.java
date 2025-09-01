package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.dto.requestDTO.RequestSessionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.exception.NoFreePlacesException;
import com.poolapp.pool.exception.SessionOverlapException;
import com.poolapp.pool.mapper.SessionMapper;
import com.poolapp.pool.model.Pool;
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
import java.util.stream.Collectors;

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
        Session session = getExistingSession(sessionDTO.getPoolName(), sessionDTO.getStartTime());

        if (session.getCurrentCapacity() <= 0) {
            throw new NoFreePlacesException(ErrorMessages.NO_FREE_PLACES);
        }
        return true;
    }

    @Override
    public SessionDTO createSession(SessionDTO sessionDTO) {
        String poolName = sessionDTO.getPoolName();

        Specification<Session> overlapSpec = sessionSpecificationBuilder.hasTimeOverlap(poolName, sessionDTO.getStartTime(), sessionDTO.getEndTime());
        boolean hasOverlap = sessionRepository.count(overlapSpec) > 0;

        if (hasOverlap) {
            throw new SessionOverlapException(ErrorMessages.EMAIL_IS_TAKEN);
        }

        Pool pool = poolRepository.findByName(poolName)
                .orElseThrow(() -> new ModelNotFoundException(ApiErrorCode.NOT_FOUND, poolName));

        Session session = sessionMapper.toEntity(sessionDTO);
        session.setPool(pool);
        session.setCurrentCapacity(pool.getMaxCapacity());

        Session saved = sessionRepository.save(session);
        return sessionMapper.toDto(saved);
    }

    public List<SessionDTO> createSessions(List<SessionDTO> dtos) {
        return dtos.stream()
                .map(this::createSession)
                .collect(Collectors.toList());
    }

    @Override
    public void changeSessionCapacity(ChangeSessionCapacityRequest request) {
        SessionDTO sessionDTO = request.getSessionDTO();
        Session session = getExistingSession(sessionDTO.getPoolName(), sessionDTO.getStartTime());

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
    public SessionDTO updateSession(RequestSessionDTO dto) {
        Session session = getExistingSession(dto.getRequestPoolDTO().getName(), dto.getStartTime());

        Session incoming = sessionMapper.toEntity(dto);
        sessionMapper.updateSessionWith(session, incoming);
        session.setPool(poolService.getPoolByName(dto.getRequestPoolDTO().getName()));

        Session updated = sessionRepository.save(session);
        return sessionMapper.toDto(updated);
    }

    @Override
    public void deleteSession(RequestSessionDTO dto) {
        Session session = getExistingSession(dto.getRequestPoolDTO().getName(), dto.getStartTime());
        sessionRepository.delete(session);
    }


    @Override
    public List<SessionDTO> findSessionsByFilter(RequestSessionDTO filterDTO) {
        Session filter = sessionMapper.toEntity(filterDTO);
        Specification<Session> spec = sessionSpecificationBuilder.buildSpecification(filter);
        return sessionRepository.findAll(spec).stream().map(sessionMapper::toDto).toList();
    }

    private Session getExistingSession(String poolName, LocalDateTime startTime) {
        return sessionRepository.findByPoolNameAndStartTime(poolName, startTime)
                .orElseThrow(() -> {
                    String details = String.format("pool=%s, startTime=%s", poolName, startTime);
                    return new ModelNotFoundException(ApiErrorCode.NOT_FOUND, details);
                });
    }

}
