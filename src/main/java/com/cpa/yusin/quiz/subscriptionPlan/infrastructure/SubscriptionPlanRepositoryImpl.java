package com.cpa.yusin.quiz.subscriptionPlan.infrastructure;

import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SubscriptionPlanRepositoryImpl implements SubscriptionPlanRepository
{
    private final SubscriptionPlanJpaRepository subscriptionPlanJpaRepository;

    public SubscriptionPlanRepositoryImpl(SubscriptionPlanJpaRepository subscriptionPlanJpaRepository)
    {
        this.subscriptionPlanJpaRepository = subscriptionPlanJpaRepository;
    }


    @Override
    public SubscriptionPlan save(SubscriptionPlan subscriptionPlan)
    {
        return subscriptionPlanJpaRepository.save(subscriptionPlan);
    }

    @Override
    public Optional<SubscriptionPlan> findById(Long id)
    {
        return subscriptionPlanJpaRepository.findById(id);
    }

    @Override
    public List<SubscriptionPlan> findAll()
    {
        return subscriptionPlanJpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id)
    {
        subscriptionPlanJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id)
    {
        return subscriptionPlanJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByDurationMonth(Integer durationMonth)
    {
        return subscriptionPlanJpaRepository.existsByDurationMonth(durationMonth);
    }

    @Override
    public boolean existsByDurationMonthAndIdNot(Integer durationMonth, Long id)
    {
        return subscriptionPlanJpaRepository.existsByDurationMonthAndIdNot(durationMonth, id);
    }


}
