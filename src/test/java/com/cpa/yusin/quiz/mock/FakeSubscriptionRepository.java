package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscription.service.port.SubscriptionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeSubscriptionRepository implements SubscriptionRepository
{
    private final AtomicLong autoGeneratedId = new AtomicLong(1);
    private final List<Subscription> subscriptionRepository = Collections.synchronizedList(new ArrayList<>());


    @Override
    public Subscription save(Subscription subscription)
    {
        if(subscription.getId() == null || subscription.getId() == 0)
        {
            Subscription newSubscription = Subscription.builder()
                    .id(autoGeneratedId.getAndIncrement())
                    .plan(subscription.getPlan())
                    .payment(subscription.getPayment())
                    .status(subscription.getStatus())
                    .build();

            subscriptionRepository.add(newSubscription);
            return newSubscription;
        } else{
            subscriptionRepository.removeIf(data -> data.getId().equals(subscription.getId()));
            subscriptionRepository.add(subscription);
            return subscription;
        }
    }

    @Override
    public Optional<Subscription> findById(long id)
    {
        return subscriptionRepository.stream()
                .filter(data -> data.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Subscription> findByMemberId(long memberId)
    {
        return subscriptionRepository.stream()
                .filter(data -> data.getMember().getId().equals(memberId))
                .findFirst();
    }
}
