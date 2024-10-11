package com.cpa.yusin.quiz.payment.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.payment.domain.type.PaymentProvider;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
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

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(unique = true)
    private String portOnePaymentId; // imp_uid // 포트원 거래 고유 번호

    @Column(nullable = false, unique = true, updatable = false)
    private String merchantUid;

    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider;

    private String failureReason;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Subscription subscription;

    public static Payment initiate(BigDecimal amount, String merchantId)
    {
        Payment payment = new Payment();
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING;
        payment.merchantUid = merchantId;
        return payment;
    }

    public void completePayment(PaymentStatus status, String failureReason, String portOnePaymentId, BigDecimal paidAmount, String paymentProvider)
    {
        this.status = status;
        this.failureReason = failureReason;
        this.portOnePaymentId = portOnePaymentId;
        this.paidAmount = paidAmount;
        this.paymentProvider = PaymentProvider.getPaymentProvider(paymentProvider);
    }
}
