package com.poolapp.pool.repository.base.criteria;

import com.poolapp.pool.model.Session;
import com.poolapp.pool.repository.custom.SessionRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SessionCriteria implements SessionRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Session> findSessionsByFilter(String poolName, LocalDateTime startTime, LocalDateTime endTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Session> query = cb.createQuery(Session.class);
        Root<Session> sessionRoot = query.from(Session.class);

        Join<Object, Object> poolJoin = sessionRoot.join("pool");

        List<Predicate> predicates = new ArrayList<>();

        if (poolName != null && !poolName.isBlank()) {
            predicates.add(
                    cb.equal(
                            cb.lower(poolJoin.get("name")),
                            poolName.toLowerCase()
                    )
            );
        }

        if (startTime != null) {
            predicates.add(
                    cb.equal(sessionRoot.get("startTime"), startTime)
            );
        }

        if (endTime != null) {
            predicates.add(
                    cb.equal(sessionRoot.get("endTime"), endTime)
            );
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}

