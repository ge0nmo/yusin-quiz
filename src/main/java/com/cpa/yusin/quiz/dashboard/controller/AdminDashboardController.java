package com.cpa.yusin.quiz.dashboard.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.DashboardResponse;
import com.cpa.yusin.quiz.content.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminContentService contentService;

    @GetMapping
    public GlobalResponse<DashboardResponse> getDashboard() {
        return GlobalResponse.success(contentService.getDashboard());
    }
}
