package com.cpa.yusin.quiz.subscription.infrastructure;

import com.cpa.yusin.quiz.subscription.domain.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, Long>
{
    @Query("SELECT s FROM Subscription s " +
            "JOIN FETCH s.member m " +
            "WHERE s.member.id = :memberId " +
            "ORDER BY s.createdAt DESC " +
            "LIMIT 1 ")
    Optional<Subscription> findTopByMemberId(@Param("memberId") long memberId);

    @Query("SELECT s FROM Subscription s " +
            "JOIN FETCH s.payment p " +
            "JOIN FETCH s.plan sp " +
            "WHERE s.member.id = :memberId " +
            "ORDER BY s.createdAt ")
    Page<Subscription> findAllByMemberId(@Param("memberId") long memberId, Pageable pageable);
}
