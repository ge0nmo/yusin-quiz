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
    @Override
    public Subscription save(Subscription subscription)
    {
        return null;
    }

    @Override
    public Optional<Subscription> findById(long id)
    {
        return Optional.empty();
    }

    @Override
    public Optional<Subscription> findByMerchantId(String merchantId)
    {
        return Optional.empty();
    }

}
