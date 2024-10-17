package com.cpa.yusin.quiz.subscription.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Subscription extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDateTime startDate;

    private LocalDateTime expiredDate;

    private LocalDateTime cancelledAt;

    public static Subscription initiate(Member member, SubscriptionPlan plan, Payment payment)
    {
        Subscription subscription = new Subscription();
        subscription.member = member;
        subscription.plan = plan;
        subscription.payment = payment;
        subscription.status = SubscriptionStatus.PENDING;

        return subscription;
    }

    public void activeSubscription(int durationMonth, LocalDateTime now)
    {
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = now;
        this.expiredDate = now.plusMonths(durationMonth);
    }

    public void expireSubscriptionStatus(LocalDateTime now)
    {
        if(this.startDate != null && this.expiredDate != null){
            if(this.expiredDate.isBefore(now) && !this.status.equals(SubscriptionStatus.EXPIRED) && !Role.ADMIN.equals(member.getRole())){
                this.status = SubscriptionStatus.EXPIRED;
                this.member.changeRole(Role.USER);
            }
        }
    }


    public void cancel()
    {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
}
