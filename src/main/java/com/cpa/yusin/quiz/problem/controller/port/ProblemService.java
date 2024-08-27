package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProblemService
{
    List<ProblemCreateResponse> save(long examId, List<ProblemCreateRequest> requests);

    void update(List<ProblemUpdateRequest> requests);

    List<ProblemResponse> getAllByExamId(long examId);

    ProblemDTO getById(long id);

    ProblemDomain findById(long id);
}
