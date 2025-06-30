package com.poolapp.pool.service;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.model.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionService {

    SessionDTO createSession(SessionDTO sessionDTO);

    void changeSessionCapacity(SessionDTO sessionDTO, int delta);

    List<SessionDTO> findSessionsByFilter(SessionDTO sessionDTO);

    boolean validateSessionHasAvailableSpots(SessionDTO sessionDTO);

    Optional<Session> getSessionByPoolNameAndStartTime(String poolName, LocalDateTime startTime);


}

