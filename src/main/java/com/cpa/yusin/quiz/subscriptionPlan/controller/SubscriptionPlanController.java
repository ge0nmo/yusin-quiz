package com.cpa.yusin.quiz.subscriptionPlan.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import com.cpa.yusin.quiz.subscriptionPlan.controller.port.SubscriptionPlanService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/plan")
@RequiredArgsConstructor
@RestController
public class SubscriptionPlanController
{
    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<SubscriptionPlanDTO>> getById(@Positive @PathVariable("id") Long id)
    {
        SubscriptionPlanDTO response = subscriptionPlanService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<SubscriptionPlanDTO>>> getAllSubscriptionPlans()
    {
        List<SubscriptionPlanDTO> response = subscriptionPlanService.getAll();

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

}
