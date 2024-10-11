package com.cpa.yusin.quiz.subscription.service.port;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.SubscriptionException;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SubscriptionValidator
{
    private final SubscriptionRepository subscriptionRepository;
    private final ClockHolder clockHolder;

    public void validateSubscription(long memberId)
    {
        Optional<Subscription> optionalSubscription = subscriptionRepository.findByMemberId(memberId);

        if(optionalSubscription.isEmpty()){
            return;
        }

        Subscription subscription = optionalSubscription.get();
        if(SubscriptionStatus.ACTIVE.equals(subscription.getStatus()) && subscription.getExpiredDate().isAfter(clockHolder.getCurrentDateTime())){

            throw new SubscriptionException(ExceptionMessage.SUBSCRIPTION_EXISTS);
        }
    }
}
