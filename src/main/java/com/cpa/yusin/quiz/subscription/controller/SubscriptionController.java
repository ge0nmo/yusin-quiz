package com.cpa.yusin.quiz.subscription.controller;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.controller.port.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscribe")
@RestController
public class SubscriptionController
{
    private final SubscriptionService subscriptionService;

    @PostMapping("/plan/{subscriptionPlanId}")
    public ResponseEntity<?> subscribe(@PathVariable("subscriptionPlanId") long subscriptionPlanId,
                                       @AuthenticationPrincipal MemberDetails memberDetails)
    {
        SubscriptionCreateResponse response = subscriptionService.initiateSubscription(memberDetails.getMember(), subscriptionPlanId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
