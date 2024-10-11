package com.cpa.yusin.quiz.subscription.infrastructure;

import com.cpa.yusin.quiz.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, Long>
{
    @Query("SELECT s FROM Subscription s " +
            "WHERE s.member.id = :memberId " +
            "ORDER BY s.createdAt DESC " +
            "LIMIT 1 ")
    Optional<Subscription> findTopByMemberId(@Param("memberId") long memberId);
}
