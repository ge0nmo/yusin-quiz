package com.cpa.yusin.quiz.subscriptionPlan.infrastructure;

import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionPlanJpaRepository extends JpaRepository<SubscriptionPlan, Long>
{
    @Query("SELECT p FROM SubscriptionPlan p WHERE p.durationMonth = :month ")
    Optional<SubscriptionPlan> findByDurationMonth(@Param("month") Integer month);

    boolean existsByDurationMonth(@Param("durationMonth") Integer durationMonth);

    boolean existsByDurationMonthAndIdNot(@Param("durationMonth") Integer durationMonth, @Param("id") Long id);
}
