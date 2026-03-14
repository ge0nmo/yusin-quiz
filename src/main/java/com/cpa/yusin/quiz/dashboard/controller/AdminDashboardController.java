package com.cpa.yusin.quiz.dashboard.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardResponse;
import com.cpa.yusin.quiz.dashboard.controller.port.DashboardService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<GlobalResponse<DashboardResponse>> getDashboard(
            @RequestParam(value = "subjectId", required = false) @Positive Long subjectId,
            @RequestParam(value = "examId", required = false) @Positive Long examId
    ) {
        DashboardResponse response = dashboardService.getDashboard(subjectId, examId);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }
}
