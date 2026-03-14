package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.problem.controller.dto.response.AdminProblemSearchResponse;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchAdminProblemV2Service {
    Page<AdminProblemSearchResponse> search(Pageable pageable, AdminProblemSearchCondition searchCondition);
}
