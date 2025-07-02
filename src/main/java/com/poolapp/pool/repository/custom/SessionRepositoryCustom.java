package com.poolapp.pool.repository.custom;

import com.poolapp.pool.model.Session;

import java.util.List;

public interface SessionRepositoryCustom {

    List<Session> findSessionsByFilter(Session filter);

}
