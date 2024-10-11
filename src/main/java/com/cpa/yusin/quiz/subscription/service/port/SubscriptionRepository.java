package com.cpa.yusin.quiz.subscription.service.port;

import com.cpa.yusin.quiz.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository
{
    Subscription save(Subscription subscription);

    Optional<Subscription> findById(long id);

    Optional<Subscription> findByMemberId(long memberId);
}