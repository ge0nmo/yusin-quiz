package com.cpa.yusin.quiz.subscriptionPlan.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class SubscriptionPlan extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer durationMonth;

    @Column(nullable = false)
    private BigDecimal price;

    public void update(SubscriptionPlanUpdateRequest request)
    {
        this.durationMonth = request.getDurationMonth();
        this.price = request.getPrice();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionPlan subscriptionPlan = (SubscriptionPlan) o;
        return Objects.equals(id, subscriptionPlan.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }
}
