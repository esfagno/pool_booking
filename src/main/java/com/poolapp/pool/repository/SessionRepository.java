package com.poolapp.pool.repository;

import com.poolapp.pool.model.Session;
import com.poolapp.pool.repository.custom.SessionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer>, SessionRepositoryCustom {
    Optional<Session> findByPoolNameAndStartTime(String poolName, LocalDateTime startTime);
}
