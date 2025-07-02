package com.poolapp.pool.repository.impl;

import com.poolapp.pool.model.Session;
import com.poolapp.pool.repository.base.criteria.SessionCriteria;
import com.poolapp.pool.repository.custom.SessionRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepositoryCustom {

    private final SessionCriteria sessionCriteria;

    public List<Session> findSessionsByFilter(Session filter) {
        return sessionCriteria.findSessionsByFilter(filter.getPool().getName(), filter.getStartTime(), filter.getEndTime());
    }

}
