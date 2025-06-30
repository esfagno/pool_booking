package com.poolapp.pool.repository.base.criteria;

import com.poolapp.pool.model.Session;
import com.poolapp.pool.repository.custom.SessionRepositoryCustom;
import com.poolapp.pool.dto.SessionDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SessionCriteria implements SessionRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Session> findSessionsByFilter(Session session) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Session> query = cb.createQuery(Session.class);
        Root<Session> sessionRoot = query.from(Session.class);

        Join<Object, Object> poolJoin = sessionRoot.join("pool");

        List<Predicate> predicates = new ArrayList<>();

        if (session.getPool().getName() != null && !session.getPool().getName().isBlank()) {
            predicates.add(
                    cb.equal(
                            cb.lower(poolJoin.get("name")),
                            session.getPool().getName().toLowerCase()
                    )
            );
        }

        if (session.getStartTime() != null) {
            predicates.add(
                    cb.equal(sessionRoot.get("startTime"), session.getStartTime())
            );
        }

        if (session.getEndTime() != null) {
            predicates.add(
                    cb.equal(sessionRoot.get("endTime"), session.getEndTime())
            );
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}

