package com.poolapp.pool.repository;

import com.poolapp.pool.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByPoolIdAndStartTimeBetween(Long poolId, LocalDateTime start, LocalDateTime end);
    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}


