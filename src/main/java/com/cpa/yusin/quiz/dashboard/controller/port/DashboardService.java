package com.cpa.yusin.quiz.dashboard.controller.port;

import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(Long subjectId, Long examId);
}
