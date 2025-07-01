package com.poolapp.pool.repository.custom;

import com.poolapp.pool.model.Session;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepositoryCustom {

    List<Session> findSessionsByFilter(String poolName, LocalDateTime startTime, LocalDateTime endTime);

}
