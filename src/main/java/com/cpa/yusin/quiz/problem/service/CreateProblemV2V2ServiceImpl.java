package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;
import com.cpa.yusin.quiz.problem.controller.port.CreateProblemV2Service;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateProblemV2V2ServiceImpl implements CreateProblemV2Service
{

    private final ProblemRepository problemRepository;
    private final ExamService examService;
    private final ChoiceService choiceService;
    private final ProblemValidator problemValidator;

    @Override
    public void saveOrUpdateV2(long examId, ProblemSaveV2Request request)
    {
        // 1. 생성(Create) 로직
        if (request.isNew()) {
            Exam exam = examService.findById(examId);
            problemValidator.validateUniqueProblemNumber(examId, request.getNumber());

            // 정적 팩토리 메서드 호출 (V2용)
            Problem problem = Problem.fromSaveOrUpdate(
                    request.getContent(),
                    request.getExplanation(),
                    request.getNumber(),
                    exam
            );

            problemRepository.save(problem);
            choiceService.saveOrUpdate(request.getChoices(), problem);

            log.info("V2 Created Problem: ID={}", problem.getId());
        }
        // 2. 수정(Update) 로직
        else {
            Problem problem = problemRepository.findById(request.getId())
                    .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));

            // 더티 체킹(Dirty Checking)으로 업데이트
            problem.update(
                    request.getContent(),
                    request.getNumber(),
                    request.getExplanation()
            );

            // Choice 업데이트 로직 위임
            choiceService.saveOrUpdate(request.getChoices(), problem);

            log.info("V2 Updated Problem: ID={}", problem.getId());
        }
    }
}