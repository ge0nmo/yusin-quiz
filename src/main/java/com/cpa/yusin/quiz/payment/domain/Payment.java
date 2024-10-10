package com.cpa.yusin.quiz.payment.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Payment extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String portOnePaymentId; // imp_uid // 포트원 거래 고유 번호

    private String merchantUid;

    private String failureReason;

    @OneToOne(mappedBy = "payment")
    private Subscription subscription;

    public static Payment initiate(BigDecimal amount, String merchantId)
    {
        Payment payment = new Payment();
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING;
        payment.merchantUid = merchantId;
        return payment;
    }

    public void completePayment(PaymentStatus status, String failureReason, String portOnePaymentId)
    {
        this.status = status;
        this.failureReason = failureReason;
        this.portOnePaymentId = portOnePaymentId;
    }
}
