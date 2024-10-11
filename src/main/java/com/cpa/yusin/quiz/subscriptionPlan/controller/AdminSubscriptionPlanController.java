package com.cpa.yusin.quiz.subscriptionPlan.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanRegisterRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanUpdateRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanRegisterResponse;
import com.cpa.yusin.quiz.subscriptionPlan.controller.port.SubscriptionPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/admin/plan")
@RequiredArgsConstructor
@RestController
public class AdminSubscriptionPlanController
{
    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    public ResponseEntity<GlobalResponse<SubscriptionPlanRegisterResponse>> save(@Valid @RequestBody SubscriptionPlanRegisterRequest request)
    {
        SubscriptionPlanRegisterResponse response = subscriptionPlanService.save(request);


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GlobalResponse<>(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GlobalResponse<SubscriptionPlanDTO>> update(@Positive @PathVariable("id") Long id,
                                                                      @Validated @RequestBody SubscriptionPlanUpdateRequest subscriptionPlanUpdateRequest)
    {
        subscriptionPlanService.update(id, subscriptionPlanUpdateRequest);
        SubscriptionPlanDTO response = subscriptionPlanService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<?>> deleteById(@Positive @PathVariable("id") Long id)
    {
        subscriptionPlanService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}
