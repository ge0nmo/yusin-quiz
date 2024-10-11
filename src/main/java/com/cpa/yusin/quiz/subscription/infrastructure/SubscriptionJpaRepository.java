package com.cpa.yusin.quiz.subscription.infrastructure;

import com.cpa.yusin.quiz.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, Long>
{

}
