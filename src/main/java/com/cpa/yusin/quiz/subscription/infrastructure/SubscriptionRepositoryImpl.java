package com.cpa.yusin.quiz.subscription.infrastructure;

import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository
{
    private final SubscriptionJpaRepository subscriptionJpaRepository;

    @Override
    public Subscription save(Subscription subscription)
    {
        return subscriptionJpaRepository.save(subscription);
    }

    @Override
    public Optional<Subscription> findById(long id)
    {
        return subscriptionJpaRepository.findById(id);
    }

    @Override
    public Optional<Subscription> findTopByMemberId(long memberId)
    {
        return subscriptionJpaRepository.findTopByMemberId(memberId);
    }


}
