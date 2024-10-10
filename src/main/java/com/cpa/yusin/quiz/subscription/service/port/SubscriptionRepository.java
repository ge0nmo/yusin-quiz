package com.cpa.yusin.quiz.subscription.service.port;

import com.cpa.yusin.quiz.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository
{
    Subscription save(Subscription subscription);

    Optional<Subscription> findById(long id);

    @Query("SELECT s FROM Subscription s " +
            "JOIN FETCH Payment p ON s.payment.id = p.id " +
            "JOIN FETCH SubscriptionPlan sp ON sp.id = s.plan.id")
    Optional<Subscription> findByMerchantId(String merchantId);
}
