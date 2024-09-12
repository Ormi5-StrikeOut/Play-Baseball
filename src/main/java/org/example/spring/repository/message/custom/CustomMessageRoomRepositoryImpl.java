package org.example.spring.repository.message.custom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.spring.domain.message.MessageMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class CustomMessageRoomRepositoryImpl implements CustomMessageRoomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<MessageMember> findQueryMessageRoom(Long memberId, Pageable pageable) {
        String query = "SELECT mm FROM MessageMember mm WHERE mm.member.id = :memberId";

        TypedQuery<MessageMember> typedQuery = entityManager.createQuery(query, MessageMember.class);
        typedQuery.setParameter("memberId", memberId);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<MessageMember> results = typedQuery.getResultList();
        long count = getCountForMember(memberId);

        return new PageImpl<>(results, pageable, count);
    }

    private long getCountForMember(Long memberId) {
        String countQuery = "SELECT COUNT(mm) FROM MessageMember mm WHERE mm.member.id = :memberId";
        TypedQuery<Long> typedCountQuery = entityManager.createQuery(countQuery, Long.class);
        typedCountQuery.setParameter("memberId", memberId);
        return typedCountQuery.getSingleResult();
    }
}
