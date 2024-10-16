package com.cpa.yusin.quiz.subscription.service.port;

import com.cpa.yusin.quiz.subscription.domain.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SubscriptionRepository
{
    Subscription save(Subscription subscription);

    Optional<Subscription> findById(long id);

    Optional<Subscription> findTopByMemberId(long memberId);

    Page<Subscription> findAllByMemberId(long memberId, Pageable pageable);
}
