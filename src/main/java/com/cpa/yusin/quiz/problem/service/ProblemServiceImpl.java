package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProblemServiceImpl implements ProblemService
{
    private final ProblemRepository problemRepository;
    private final ExamService examService;

    @Override
    public ProblemDomain save(long examId, ProblemCreateRequest problem)
    {


        return null;
    }

    @Override
    public ProblemDomain getById(long id)
    {
        return null;
    }

    @Override
    public Optional<ProblemDomain> findById(long id)
    {
        return Optional.empty();
    }
}
